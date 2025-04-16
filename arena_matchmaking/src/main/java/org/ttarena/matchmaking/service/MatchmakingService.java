package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class MatchmakingService {

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final MatchFoundPublisher matchFoundPublisher;

    @Autowired
    public MatchmakingService(MatchFoundPublisher matchFoundPublisher) {
        this.matchFoundPublisher = matchFoundPublisher;
    }

    public Mono<Void> enqueueUser(String userId) {
        waitingUsers.add(userId);
        log.info("User {} added to matchmaking queue. Current size: {}", userId, waitingUsers.size());

        if (waitingUsers.size() >= 2) {
            String user1 = waitingUsers.poll();
            String user2 = waitingUsers.poll();
            log.info("Match created between {} and {}", user1, user2);

            Map<String, Object> matchEvent = new HashMap<>();
            matchEvent.put("type", "MATCH_FOUND");
            matchEvent.put("players", List.of(user1, user2));
            matchEvent.put("timestamp", Instant.now().toString());

            return matchFoundPublisher.publishMatch(matchEvent);
        }

        return Mono.empty();
    }

    public Mono<Void> dequeueUser(String userId) {
        boolean removed = waitingUsers.remove(userId);
        if (removed) {
            log.info("User {} removed from matchmaking queue.", userId);
        } else {
            log.warn("Attempted to remove user {} who was not in the queue.", userId);
        }
        return Mono.empty();
    }
}
