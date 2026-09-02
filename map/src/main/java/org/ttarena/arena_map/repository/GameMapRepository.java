package org.ttarena.arena_map.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_map.document.GameMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GameMapRepository extends ReactiveMongoRepository<GameMap, String> {

    Flux<GameMap> findByOwnerId(String ownerId);

    Mono<GameMap> findByIdAndOwnerId(String id, String ownerId);

    Flux<GameMap> findByNameContainingIgnoreCase(String name);

    Mono<Long> countByOwnerId(String ownerId);
}
