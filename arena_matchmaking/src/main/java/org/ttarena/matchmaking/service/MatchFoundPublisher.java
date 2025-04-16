package org.ttarena.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchFoundPublisher {

    private final ReactiveRedisTemplate<String, Map<String, Object>> redisTemplate;

    public Mono<Void> publishMatch(Map<String, Object> matchEvent) {
        return redisTemplate.convertAndSend("match.found", matchEvent)
                .doOnNext(count -> log.info("Published match event to {} subscribers: {}", "match.found", matchEvent))
                .then();
    }
}
