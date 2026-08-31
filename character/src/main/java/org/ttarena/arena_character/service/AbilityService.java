package org.ttarena.arena_character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ttarena.arena_character.exception.BadRequestException;
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

@Service
public class AbilityService {

    private final AbilityRepository abilityRepository;
    private final CharacterRepository characterRepository;

    @Autowired
    public AbilityService(AbilityRepository abilityRepository, CharacterRepository characterRepository) {
        this.abilityRepository = abilityRepository;
        this.characterRepository = characterRepository;
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

    /**
     * Resolves a single ability cast: spends the caster's resource, computes
     * the damage/healing amount from the caster's stats, applies it to every
     * resolved target (clamped to 0/maxHealth), persists every character
     * touched, and returns a summary of what happened.
     *
     * Targeting is caller-driven: for SELF abilities the provided targetIds
     * are ignored and the caster is the only target; for every other
     * targetType the caller supplies the relevant target id(s) (ally/enemy
     * distinction and turn/session state are outside this service's scope).
     */
    public Mono<CombatResult> castAbility(String casterId, String abilityId, List<String> targetIds) {
        Mono<Character> casterMono = characterRepository.findById(casterId)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + casterId)));
        Mono<Ability> abilityMono = getAbilityById(abilityId);

        return casterMono.zipWith(abilityMono)
                .flatMap(tuple -> resolveCast(tuple.getT1(), tuple.getT2(), targetIds));
    }

    private Mono<CombatResult> resolveCast(Character caster, Ability ability, List<String> targetIds) {
        if (ability.getResourceType() != caster.getPowerResourceType()) {
            return Mono.error(new BadRequestException(
                    caster.getName() + " cannot use " + ability.getName()
                            + ": it costs " + ability.getResourceType()
                            + " but this character uses " + caster.getPowerResourceType() + "."));
        }

        if (caster.getPowerResourceAmount() < ability.getResourceCost()) {
            return Mono.error(new BadRequestException(
                    caster.getName() + " does not have enough " + ability.getResourceType()
                            + " to cast " + ability.getName()
                            + " (needs " + ability.getResourceCost()
                            + ", has " + caster.getPowerResourceAmount() + ")."));
        }

        List<String> resolvedTargetIds = ability.getTargetType() == TargetType.SELF
                ? List.of(caster.getId())
                : targetIds;

        if (resolvedTargetIds == null || resolvedTargetIds.isEmpty()) {
            return Mono.error(new BadRequestException(ability.getName() + " requires at least one target."));
        }

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
                    int actualAmount = switch (ability.getAbilityType()) {
                        case DAMAGE -> applyMitigatedDamage(target, effectAmount);
                        case HEAL -> target.applyHealing(effectAmount);
                        // BUFF/DEBUFF are modeled as a type for future extension but don't
                        // move health yet - no status-effect system exists in this service.
                        default -> 0;
                    };
                    return characterRepository.save(target)
                            .map(saved -> new TargetOutcome(
                                    saved.getId(), saved.getName(), actualAmount, saved.getHealth(), !saved.isAlive()));
                });
    }

    /**
     * Flat armor mitigation: armor / (armor + 400), capped at 75% reduction.
     * A plate-wearer (armor 200) mitigates ~33%, cloth (armor 50) ~11%.
     * At least 1 damage always gets through.
     */
    private int applyMitigatedDamage(Character target, int rawAmount) {
        double mitigation = Math.min(0.75, target.getArmor() / (double) (target.getArmor() + 400));
        int mitigatedAmount = (int) Math.round(rawAmount * (1 - mitigation));
        return target.applyDamage(Math.max(mitigatedAmount, 1));
    }
}
