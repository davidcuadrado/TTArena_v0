package org.ttarena.arena_ability.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbilityRepository extends MongoRepository<Ability, String> {

    Optional<Ability> findByNameIgnoreCase(String name);

    List<Ability> findByWowClass(WowClass wowClass);

    List<Ability> findBySpecialization(Specialization specialization);

    @Query("{ $or: [ { 'wowClass': ?0 }, { 'specialization': { $in: ?1 } } ] }")
    List<Ability> findByClassOrSpecializations(WowClass wowClass, List<Specialization> specializations);

    List<Ability> findByAbilityType(org.ttarena.arena_ability.model.AbilityType abilityType);

    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Ability> findByNameOrDescriptionContaining(String searchText);

    List<Ability> findByCooldownLessThanEqual(int maxCooldown);

    List<Ability> findByResourceCostLessThanEqual(int maxResourceCost);
}

