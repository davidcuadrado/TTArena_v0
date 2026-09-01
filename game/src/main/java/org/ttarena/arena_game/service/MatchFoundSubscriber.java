package org.ttarena.arena_game.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Service;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.MatchFoundEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Turns a {@code match.found} event into a game session. This is the consumer
 * matchmaking has been publishing to.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class MatchFoundSubscriber {

    private static final String MATCH_FOUND_TOPIC = "match.found";

    private final ReactiveRedisMessageListenerContainer container;
    private final GameSessionService gameSessionService;
    private final ObjectMapper objectMapper;

    private volatile Disposable subscription;

    public MatchFoundSubscriber(ReactiveRedisMessageListenerContainer container,
                                GameSessionService gameSessionService,
                                ObjectMapper objectMapper) {
        this.container = container;
        this.gameSessionService = gameSessionService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            return;
        }

        log.info("Subscribing to Redis channel '{}'", MATCH_FOUND_TOPIC);

        this.subscription = container.receive(
                        Collections.singletonList(new ChannelTopic(MATCH_FOUND_TOPIC)),
                        RedisSerializationContext.string().getKeySerializationPair(),
                        RedisSerializationContext.string().getValueSerializationPair())
                .flatMap(message -> handle(message.getMessage()))
                .doOnError(error -> log.warn("Subscription to '{}' failed: {}", MATCH_FOUND_TOPIC, error.getMessage()))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30))
                        .transientErrors(true))
                .subscribe(
                        session -> { },
                        error -> log.error("Subscription to '{}' terminated: {}",
                                MATCH_FOUND_TOPIC, error.getMessage(), error));
    }

    private Mono<GameSession> handle(String rawMessage) {
        log.info("Received match.found: {}", rawMessage);
        try {
            MatchFoundEvent event = objectMapper.readValue(rawMessage, MatchFoundEvent.class);

            if (event.getParticipants() == null || event.getParticipants().size() != 2) {
                log.warn("Ignoring match.found without exactly two participants: {}", rawMessage);
                return Mono.empty();
            }

            List<GameSession.Participant> participants = event.getParticipants().stream()
                    .map(participant -> new GameSession.Participant(
                            participant.getUserId(), participant.getCharacterId()))
                    .toList();

            return gameSessionService.startSession(participants)
                    .onErrorResume(e -> {
                        log.error("Could not start a session for {}: {}", rawMessage, e.getMessage(), e);
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.error("Could not read match.found payload: {}", e.getMessage(), e);
            return Mono.empty();
        }
    }

    @PreDestroy
    public void unsubscribe() {
        Disposable current = this.subscription;
        if (current != null && !current.isDisposed()) {
            current.dispose();
        }
    }
}
