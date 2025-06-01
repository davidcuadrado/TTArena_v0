package org.ttarena.arena_user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisPublisherService {

    private final ReactiveRedisTemplate<String, Map<String, Object>> redisTemplate;

    @Autowired
    public RedisPublisherService(ReactiveRedisTemplate<String, Map<String, Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> publishUserEvent(String eventType, String userId) {
        String topic = "user.status." + userId;

        Map<String, Object> message = new HashMap<>();
        message.put("type", eventType);
        message.put("userId", userId);
        message.put("timestamp", Instant.now().toString());

        return redisTemplate.convertAndSend(topic, message);
    }
}
