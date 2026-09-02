package org.ttarena.arena_user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;
import org.ttarena.arena_user.util.RedisEvent;
import org.ttarena.arena_user.util.UserEventType;

import java.time.Instant;

/**
 * Publishes user status events on {@code user.status.<userId>}.
 *
 * <p>The payload is a JSON string, which is what the matchmaking service
 * subscribes with (string serializers on both key and value).
 */
@Slf4j
@Service
public class RedisPublisherService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisPublisherService(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * @return the number of subscribers that received the event.
     */
    public Mono<Long> publishUserEvent(UserEventType eventType, String userId, String characterId) {
        return publishUserEvent(eventType.name(), userId, characterId);
    }

    /**
     * @return the number of subscribers that received the event.
     */
    public Mono<Long> publishUserEvent(String eventType, String userId, String characterId) {
        String topic = "user.status." + userId;
        RedisEvent event = new RedisEvent(eventType, userId, characterId, Instant.now());

        // fromCallable keeps a serialization failure inside the reactive stream
        // instead of throwing out of this method.
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(payload -> redisTemplate.convertAndSend(topic, payload))
                .doOnNext(count -> log.info("Published {} for user {} to {} subscriber(s)", eventType, userId, count))
                .doOnError(e -> log.error("Failed to publish {} for user {}: {}",
                        eventType, userId, e.getMessage(), e));
    }
}
