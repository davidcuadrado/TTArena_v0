package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.document.MatchFoundEvent;
import org.ttarena.matchmaking.util.UserEventType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class RedisMatchmakingService {

    private static final String WAITING_USERS_KEY = "matchmaking:waiting-users";
    private static final int MATCH_SIZE = 2;
    private static final Duration POLLING_INTERVAL = Duration.ofSeconds(1);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveSetOperations<String, String> setOperations;
    private final MatchFoundPublisher matchFoundPublisher;

    @Autowired
    public RedisMatchmakingService(
            ReactiveRedisTemplate<String, String> redisTemplate,
            MatchFoundPublisher matchFoundPublisher) {
        this.redisTemplate = redisTemplate;
        this.setOperations = redisTemplate.opsForSet();
        this.matchFoundPublisher = matchFoundPublisher;
        
        // Iniciar el proceso de matchmaking
        startMatchmakingProcess();
    }

    /**
     * Añade un usuario a la cola de espera en Redis
     */
    public Mono<Long> enqueueUser(String userId) {
        log.info("Adding user {} to matchmaking queue", userId);
        return setOperations.add(WAITING_USERS_KEY, userId)
                .doOnSuccess(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        log.info("User {} successfully added to matchmaking queue", userId);
                    } else {
                        log.warn("User {} was already in the matchmaking queue", userId);
                    }
                })
                .doOnError(error -> log.error("Error adding user {} to matchmaking queue: {}", userId, error.getMessage()));
    }

    /**
     * Elimina un usuario de la cola de espera en Redis
     */
    public Mono<Long> dequeueUser(String userId) {
        log.info("Removing user {} from matchmaking queue", userId);
        return setOperations.remove(WAITING_USERS_KEY, userId)
                .doOnSuccess(result -> {
                    if (result > 0) {
                        log.info("User {} successfully removed from matchmaking queue", userId);
                    } else {
                        log.warn("User {} was not in the matchmaking queue", userId);
                    }
                })
                .doOnError(error -> log.error("Error removing user {} from matchmaking queue: {}", userId, error.getMessage()));
    }

    /**
     * Inicia el proceso de matchmaking que se ejecuta periódicamente
     */
    private void startMatchmakingProcess() {
        Flux.interval(POLLING_INTERVAL)
                .flatMap(tick -> processMatchmaking())
                .subscribe(
                        result -> {},
                        error -> log.error("Error in matchmaking process: {}", error.getMessage())
                );
    }

    /**
     * Procesa la cola de matchmaking para crear partidas
     */
    private Mono<Void> processMatchmaking() {
        return setOperations.size(WAITING_USERS_KEY)
                .flatMap(size -> {
                    if (size >= MATCH_SIZE) {
                        return createMatch();
                    }
                    return Mono.empty();
                });
    }

    /**
     * Crea una partida con los primeros usuarios de la cola
     */
    private Mono<Void> createMatch() {
        return setOperations.pop(WAITING_USERS_KEY, MATCH_SIZE)
                .collectList()
                .filter(users -> users.size() >= MATCH_SIZE)
                .flatMap(users -> {
                    log.info("Creating match with users: {}", users);
                    
                    MatchFoundEvent matchEvent = new MatchFoundEvent(
                            UserEventType.MATCH_FOUND.name(),
                            users,
                            Instant.now()
                    );
                    
                    return matchFoundPublisher.publishMatch(matchEvent);
                });
    }
    
    /**
     * Obtiene el número de usuarios en la cola de espera
     */
    public Mono<Long> getQueueSize() {
        return setOperations.size(WAITING_USERS_KEY);
    }
    
    /**
     * Obtiene todos los usuarios en la cola de espera
     */
    public Flux<String> getWaitingUsers() {
        return setOperations.members(WAITING_USERS_KEY);
    }
}
