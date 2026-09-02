package org.ttarena.arena_user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import org.ttarena.arena_user.document.ArenaUserDocument;
import reactor.core.publisher.Mono;

@Repository
public interface ArenaUserRepository extends ReactiveMongoRepository<ArenaUserDocument, String>{

	Mono<ArenaUserDocument> findByUsername(String username);

}
