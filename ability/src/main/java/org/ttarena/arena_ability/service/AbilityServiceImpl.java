package org.ttarena.arena_ability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import org.ttarena.arena_ability.repository.AbilityRepository;
// Unused import removed: import org.ttarena.arena_ability.service.AbilityService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityServiceImpl implements AbilityServiceInterface {

    private final AbilityRepository abilityRepository;

    @Override
    public Flux<Ability> getAllAbilities() {
        log.debug("Getting all abilities");
        return abilityRepository.findAll()
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed getting all abilities"));
    }

    @Override
    public Mono<Ability> getAbilityById(String id) {
        log.debug("Getting ability by ID: {}", id);
        return abilityRepository.findById(id)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnSuccess(ability -> log.debug("Successfully completed getting ability by ID: {}", id));
    }

    @Override
    public Mono<Ability> getAbilityByName(String name) {
        log.debug("Getting ability by name: {}", name);
        return abilityRepository.findByName(name)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnSuccess(ability -> log.debug("Completed getting ability by name: {}", name));
    }

    @Override
    public Flux<Ability> getAbilitiesByClass(WowClass wowClass) {
        log.debug("Getting abilities by class: {}", wowClass);
        return abilityRepository.findByWowClass(wowClass)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed getting abilities by class: {}", wowClass));
    }

    @Override
    public Flux<Ability> getAbilitiesBySpecialization(Specialization specialization) {
        log.debug("Getting abilities by specialization: {}", specialization);
        return abilityRepository.findBySpecialization(specialization)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed getting abilities by specialization: {}", specialization));
    }

    @Override
    public Flux<Ability> getAllAbilitiesForClass(WowClass wowClass) {
        log.debug("Getting all abilities for class: {}", wowClass);

        List<Specialization> classSpecializations = getSpecializationsForClass(wowClass);

        return abilityRepository.findByWowClassOrSpecializations(wowClass, classSpecializations)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed getting all abilities for class: {}", wowClass));
    }

    @Override
    public Flux<Ability> getAbilitiesByType(AbilityType abilityType) {
        log.debug("Getting abilities by type: {}", abilityType);
        return abilityRepository.findByAbilityType(abilityType)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed getting abilities by type: {}", abilityType));
    }

    @Override
    public Flux<Ability> searchAbilities(String searchText) {
        log.debug("Searching abilities with text: {}", searchText);
        return abilityRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchText, searchText)
                .doOnNext(ability -> log.debug("Found ability: {}", ability.getName()))
                .doOnComplete(() -> log.debug("Completed searching abilities with text: {}", searchText));
    }

    @Override
    public Mono<Ability> createAbility(Ability ability) {
        log.debug("Creating ability: {}", ability.getName());

        return abilityRepository.existsByName(ability.getName())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Ability with name '{}' already exists", ability.getName());
                        return Mono.error(new IllegalArgumentException("Ability with name '" + ability.getName() + "' already exists"));
                    }
                    return abilityRepository.save(ability)
                            .doOnSuccess(savedAbility -> log.info("Created ability: {}", savedAbility.getName()));
                });
    }

    @Override
    public Mono<Ability> updateAbility(String id, Ability ability) {
        log.debug("Updating ability with ID: {}", id);

        return abilityRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Ability not found with ID: " + id)))
                .flatMap(existingAbility -> {
                    // Update fields
                    existingAbility.setName(ability.getName());
                    existingAbility.setDescription(ability.getDescription());
                    existingAbility.setIconUrl(ability.getIconUrl());
                    existingAbility.setCooldown(ability.getCooldown());
                    existingAbility.setCastTime(ability.getCastTime());
                    existingAbility.setResourceCost(ability.getResourceCost());
                    existingAbility.setResourceType(ability.getResourceType());
                    existingAbility.setAbilityType(ability.getAbilityType());
                    existingAbility.setWowClass(ability.getWowClass());
                    existingAbility.setSpecialization(ability.getSpecialization());
                    existingAbility.setBaseValue(ability.getBaseValue());
                    existingAbility.setRange(ability.getRange());
                    existingAbility.setAreaEffect(ability.isAreaEffect());
                    existingAbility.setAreaRadius(ability.getAreaRadius());

                    return abilityRepository.save(existingAbility)
                            .doOnSuccess(updatedAbility -> log.info("Updated ability: {}", updatedAbility.getName()));
                });
    }

    @Override
    public Mono<Void> deleteAbility(String id) {
        log.debug("Deleting ability with ID: {}", id);

        return abilityRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Ability not found with ID: " + id)))
                .flatMap(ability -> {
                    log.info("Deleting ability: {}", ability.getName());
                    return abilityRepository.delete(ability);
                }).then();
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        log.debug("Checking if ability exists by name: {}", name);
        return abilityRepository.existsByName(name);
    }

    private List<Specialization> getSpecializationsForClass(WowClass wowClass) {
        return switch (wowClass) {
            case PALADIN -> Arrays.asList(
                    Specialization.HOLY_PALADIN,
                    Specialization.PROTECTION_PALADIN,
                    Specialization.RETRIBUTION_PALADIN
            );
            case PRIEST -> Arrays.asList(
                    Specialization.DISCIPLINE_PRIEST,
                    Specialization.HOLY_PRIEST,
                    Specialization.SHADOW_PRIEST
            );
            case ROGUE -> Arrays.asList(
                    Specialization.ASSASSINATION_ROGUE,
                    Specialization.OUTLAW_ROGUE,
                    Specialization.SUBTLETY_ROGUE
            );
            case SHAMAN -> Arrays.asList(
                    Specialization.ELEMENTAL_SHAMAN,
                    Specialization.ENHANCEMENT_SHAMAN,
                    Specialization.RESTORATION_SHAMAN
            );
            case WARRIOR -> Arrays.asList(
                    Specialization.ARMS_WARRIOR,
                    Specialization.FURY_WARRIOR,
                    Specialization.PROTECTION_WARRIOR
            );
            default -> Collections.emptyList();
        };
    }
}