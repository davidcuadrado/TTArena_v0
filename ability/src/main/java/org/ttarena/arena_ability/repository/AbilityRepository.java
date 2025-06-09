package org.ttarena.arena_ability.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AbilityRepository extends ReactiveMongoRepository<Ability, String> {

    Mono<Ability> findByName(String name);

    Flux<Ability> findByWowClass(WowClass wowClass);

    Flux<Ability> findBySpecialization(Specialization specialization);

    Flux<Ability> findByAbilityType(AbilityType abilityType);

    @Query("{ $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Flux<Ability> findByNameOrDescriptionContainingIgnoreCase(String searchText);

    Flux<Ability> findByNameOrDescriptionBothIgnoreCase(String name, String description);

    @Query("{ $or: [ " +
           "{ 'wowClass': ?0 }, " +
           "{ 'specialization': { $in: ?1 } } " +
           "] }")
    Flux<Ability> findByWowClassOrSpecializationIn(WowClass wowClass, List<Specialization> specializations);

    Mono<Boolean> existsByName(String name);
}

