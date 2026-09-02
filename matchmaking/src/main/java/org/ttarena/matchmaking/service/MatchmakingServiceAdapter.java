package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.util.RedisEvent;
import org.ttarena.matchmaking.util.UserEventType;

@Slf4j
@Service
public class MatchmakingServiceAdapter {

    private final RedisMatchmakingService redisMatchmakingService;
    private final MatchFoundPublisher matchFoundPublisher;

    @Autowired
    public MatchmakingServiceAdapter(
            RedisMatchmakingService redisMatchmakingService,
            MatchFoundPublisher matchFoundPublisher) {
        this.redisMatchmakingService = redisMatchmakingService;
        this.matchFoundPublisher = matchFoundPublisher;
    }

    /**
     * Procesa eventos de usuario recibidos a través de Redis
     */
    public Mono<Void> processUserEvent(RedisEvent event) {
        log.info("Processing user event: {}", event);
        
        if (UserEventType.USER_CONNECTED.name().equals(event.getType())) {
            return redisMatchmakingService.enqueueUser(event.getUserId())
                    .then();
        } else if (UserEventType.USER_DISCONNECTED.name().equals(event.getType())) {
            return redisMatchmakingService.dequeueUser(event.getUserId())
                    .then();
        }
        
        return Mono.empty();
    }
}
