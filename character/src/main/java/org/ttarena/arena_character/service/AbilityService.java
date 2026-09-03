package org.ttarena.arena_character.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ttarena.arena_character.combat.AbilityEffect;
import org.ttarena.arena_character.combat.AbilityEffectRegistry;
import org.ttarena.arena_character.combat.CastContext;
import org.ttarena.arena_character.combat.CastRuleChain;
import org.ttarena.arena_character.exception.NotFoundException;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.CombatResult;
import org.ttarena.arena_character.model.TargetOutcome;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.TargetType;
import org.ttarena.arena_character.repository.AbilityRepository;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class AbilityService {
    private final AbilityRepository abilityRepository;
    private final CharacterRepository characterRepository;
    private final AbilityEffectRegistry abilityEffects;
    private final CastRuleChain castRules;

    public AbilityService(AbilityRepository abilityRepository,
                          CharacterRepository characterRepository,
                          AbilityEffectRegistry abilityEffects,
                          CastRuleChain castRules) {
        this.abilityRepository = abilityRepository;
        this.characterRepository = characterRepository;
        this.abilityEffects = abilityEffects;
        this.castRules = castRules;
    }

    public Flux<Ability> getAllAbilities() {
        return abilityRepository.findAll();
    }

    public Mono<Ability> getAbilityById(String id) {
        return abilityRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any ability with id: " + id)));
    }

    public Flux<Ability> getAbilitiesByClass(CharacterClass characterClass) {
        return abilityRepository.findByCharacterClass(characterClass);
    }

    public Flux<Ability> getAbilitiesByClassAndSpecialization(CharacterClass characterClass, String specialization) {
        return abilityRepository.findByCharacterClassAndSpecialization(characterClass, specialization);
    }

    public Mono<CombatResult> castAbility(String casterId, String abilityId, List<String> targetIds, String callerId,
                                          Integer distanceToTarget) {
        Mono<Character> casterMono = characterRepository.findById(casterId)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + casterId)));
        Mono<Ability> abilityMono = getAbilityById(abilityId);

        return casterMono.zipWith(abilityMono)
                .flatMap(tuple -> resolveCast(tuple.getT1(), tuple.getT2(), targetIds, callerId, distanceToTarget));
    }

    private Mono<CombatResult> resolveCast(Character caster, Ability ability, List<String> targetIds, String callerId,
                                           Integer distanceToTarget) {
        List<String> resolvedTargetIds = ability.getTargetType() == TargetType.SELF
                ? List.of(caster.getId())
                : targetIds;

        return Mono.fromRunnable(() -> castRules.check(
                        new CastContext(caster, ability, resolvedTargetIds, callerId, distanceToTarget)))
                .then(Mono.defer(() -> applyCast(caster, ability, resolvedTargetIds)));
    }

    private Mono<CombatResult> applyCast(Character caster, Ability ability, List<String> resolvedTargetIds) {
        caster.setPowerResourceAmount(caster.getPowerResourceAmount() - ability.getResourceCost());
        int effectAmount = ability.computeEffectAmount(caster);

        return characterRepository.save(caster)
                .flatMap(savedCaster -> Flux.fromIterable(resolvedTargetIds)
                        .flatMap(targetId -> resolveTarget(targetId, ability, effectAmount))
                        .collectList()
                        .map(outcomes -> new CombatResult(
                                savedCaster.getId(),
                                savedCaster.getName(),
                                ability.getId(),
                                ability.getName(),
                                ability.getAbilityType(),
                                ability.getResourceCost(),
                                savedCaster.getPowerResourceAmount(),
                                outcomes)));
    }

    private Mono<TargetOutcome> resolveTarget(String targetId, Ability ability, int effectAmount) {
        return characterRepository.findById(targetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + targetId)))
                .flatMap(target -> {
                    int actualAmount = applyEffect(target, ability, effectAmount);
                    return characterRepository.save(target)
                            .map(saved -> new TargetOutcome(
                                    saved.getId(), saved.getName(), actualAmount, saved.getHealth(), !saved.isAlive()));
                });
    }

    private int applyEffect(Character target, Ability ability, int effectAmount) {
        return abilityEffects.forType(ability.getAbilityType())
                .map(effect -> effect.apply(target, effectAmount))
                .orElseGet(() -> {
                    log.debug("No AbilityEffect registered for type {}; '{}' had no effect on {}",
                            ability.getAbilityType(), ability.getName(), target.getName());
                    return 0;
                });
    }
}
