package org.ttarena.arena_character.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.enums.CharacterClass;
import reactor.core.publisher.Flux;

@Repository
public interface AbilityRepository extends ReactiveMongoRepository<Ability, String> {

    Flux<Ability> findByCharacterClass(CharacterClass characterClass);

    Flux<Ability> findByCharacterClassAndSpecialization(CharacterClass characterClass, String specialization);
}
