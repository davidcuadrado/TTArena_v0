package org.ttarena.arena_ability.service;

import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AbilityServiceInterface {

    Flux<Ability> getAllAbilities();

    Mono<Ability> getAbilityById(String id);

    Mono<Ability> getAbilityByName(String name);

    Flux<Ability> getAbilitiesByClass(WowClass wowClass);

    Flux<Ability> getAbilitiesBySpecialization(Specialization specialization);

    Flux<Ability> getAllAbilitiesForClass(WowClass wowClass);

    Flux<Ability> getAbilitiesByType(AbilityType abilityType);

    Flux<Ability> searchAbilities(String searchText);

    Mono<Ability> createAbility(Ability ability);

    Mono<Ability> updateAbility(String id, Ability ability);

    Mono<Void> deleteAbility(String id);

    Mono<Boolean> existsByName(String name);
}

