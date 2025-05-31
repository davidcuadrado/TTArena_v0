package org.ttarena.arena_user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_user.model.Character;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CharacterRepository extends ReactiveMongoRepository<Character, String> {

    Mono<Character> findByName(String name);

    Flux<Character> findByCharacterClass(String characterClass);
}
