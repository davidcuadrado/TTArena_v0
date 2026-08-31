package org.ttarena.matchmaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Service;
import org.ttarena.matchmaking.util.UserEventType;
import org.ttarena.matchmaking.util.RedisEvent;
import reactor.core.Disposable;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;

/**
 * Subscribes to user status events published on Redis.
 *
 * <p>The subscription is started once the application is fully up (not from
 * {@code @PostConstruct}), and connection failures are logged and retried with
 * backoff instead of being silently dropped by the reactive pipeline.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisSubscriberService {

    private static final String USER_STATUS_TOPIC = "user.status.*";

    private final ReactiveRedisMessageListenerContainer container;
    private final MatchmakingService matchmakingService;
    private final ObjectMapper objectMapper;

    private volatile Disposable subscription;

    public RedisSubscriberService(ReactiveRedisMessageListenerContainer container,
                                  MatchmakingService matchmakingService,
                                  ObjectMapper objectMapper) {
        this.container = container;
        this.matchmakingService = matchmakingService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToUserStatusEvents() {
        if (subscription != null && !subscription.isDisposed()) {
            return;
        }

        log.info("Subscribing to Redis channel pattern '{}'", USER_STATUS_TOPIC);

        this.subscription = container.receive(
                        Collections.singletonList(new ChannelTopic(USER_STATUS_TOPIC)),
                        RedisSerializationContext.string().getKeySerializationPair(),
                        RedisSerializationContext.string().getValueSerializationPair())
                .doOnNext(message -> handleMessage(message.getChannel(), message.getMessage()))
                .doOnError(error -> log.warn("Redis subscription to '{}' failed: {}",
                        USER_STATUS_TOPIC, error.getMessage()))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30))
                        .transientErrors(true))
                .subscribe(
                        message -> { /* handled in doOnNext */ },
                        error -> log.error("Redis subscription to '{}' terminated: {}",
                                USER_STATUS_TOPIC, error.getMessage(), error));
    }

    private void handleMessage(String channel, String rawMessage) {
        log.info("Received message on {}: {}", channel, rawMessage);
        try {
            RedisEvent event = objectMapper.readValue(rawMessage, RedisEvent.class);

            if (UserEventType.USER_CONNECTED.name().equals(event.getType())) {
                matchmakingService.enqueueUser(event.getUserId())
                        .subscribe(null, e -> log.error("Failed to enqueue user {}: {}",
                                event.getUserId(), e.getMessage(), e));
            } else if (UserEventType.USER_DISCONNECTED.name().equals(event.getType())) {
                matchmakingService.dequeueUser(event.getUserId())
                        .subscribe(null, e -> log.error("Failed to dequeue user {}: {}",
                                event.getUserId(), e.getMessage(), e));
            } else {
                log.debug("Ignoring event of type {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        Disposable current = this.subscription;
        if (current != null && !current.isDisposed()) {
            log.info("Cancelling Redis subscription to '{}'", USER_STATUS_TOPIC);
            current.dispose();
        }
    }
}
