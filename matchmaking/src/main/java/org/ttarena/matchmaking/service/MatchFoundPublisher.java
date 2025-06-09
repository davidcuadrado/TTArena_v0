package org.ttarena.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.document.MatchFoundEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchFoundPublisher {

    private final ReactiveRedisTemplate<String, MatchFoundEvent> redisTemplate;

    public Mono<Void> publishMatch(MatchFoundEvent event) {
        return redisTemplate.convertAndSend("match.found", event)
                .doOnNext(count -> log.info("Published MATCH_FOUND event to {} subscriber(s): {}", count, event))
                .then();
    }
}
