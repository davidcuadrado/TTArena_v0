package org.ttarena.arena_game.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GameSessionRepository extends ReactiveMongoRepository<GameSession, String> {

    Flux<GameSession> findByParticipantsUserId(String userId);

    Mono<GameSession> findFirstByParticipantsUserIdAndStatusOrderByCreatedAtDesc(String userId, GameStatus status);
}
