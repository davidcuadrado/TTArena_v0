package org.ttarena.arena_user.service;

import org.slf4j.Logger; // Recommended for logging
import org.slf4j.LoggerFactory; // Recommended for logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(RedisPublisherService.class);

    private final ReactiveRedisTemplate<String, Map<String, Object>> redisTemplate;

    private static final String KEY_EVENT_TYPE = "type";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String TOPIC_PREFIX_USER_STATUS = "user.status.";

    @Autowired
    public RedisPublisherService(ReactiveRedisTemplate<String, Map<String, Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> publishUserEvent(String eventType, String userId) {
        // Basic input validation
        if (eventType == null || eventType.isBlank()) {
            logger.warn("Event type is null or blank. Aborting Redis publish.");
            return Mono.error(new IllegalArgumentException("Event type cannot be null or blank"));
        }
        if (userId == null || userId.isBlank()) {
            logger.warn("User ID is null or blank. Aborting Redis publish.");
            return Mono.error(new IllegalArgumentException("User ID cannot be null or blank"));
        }

        String topic = TOPIC_PREFIX_USER_STATUS + userId;

        Map<String, Object> message = new HashMap<>();
        message.put(KEY_EVENT_TYPE, eventType);
        message.put(KEY_USER_ID, userId);
        message.put(KEY_TIMESTAMP, Instant.now().toString());

        return redisTemplate.convertAndSend(topic, message)
                .doOnSuccess(clientsReceived -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully published event to Redis topic '{}'. Clients received: {}", topic, clientsReceived);
                    }
                })
                .thenReturn(true) // If convertAndSend completes (emits the Long), consider it a success and return true.
                .onErrorResume(throwable -> {
                    // Log the actual error for observability
                    logger.error("Failed to publish user event to Redis topic '{}': {}", topic, throwable.getMessage(), throwable);
                    return Mono.just(false); // On any error during publishing, return false.
                });
    }
}