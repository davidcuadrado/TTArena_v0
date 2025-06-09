package org.ttarena.arena_map.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_map.document.GameMap;
import reactor.core.publisher.Flux;

@Repository
public interface GameMapRepository extends ReactiveMongoRepository<GameMap, String> {

    Flux<GameMap> findByNameContainingIgnoreCase(String name);
    Flux<GameMap> findByAuthor(String author);
}
