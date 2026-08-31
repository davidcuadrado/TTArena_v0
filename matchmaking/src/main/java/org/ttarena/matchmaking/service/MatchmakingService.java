package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.document.MatchFoundEvent;
import org.ttarena.matchmaking.util.UserEventType;

import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class MatchmakingService {

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();

    /**
     * Optional: the publisher only exists when the Redis wiring is enabled
     * (property {@code redis.enabled}). Tests run without it.
     */
    private final ObjectProvider<MatchFoundPublisher> matchFoundPublisher;

    @Autowired
    public MatchmakingService(ObjectProvider<MatchFoundPublisher> matchFoundPublisher) {
        this.matchFoundPublisher = matchFoundPublisher;
    }

    public Mono<Void> enqueueUser(String userId) {
        waitingUsers.add(userId);
        log.info("User {} added to matchmaking queue. Current size: {}", userId, waitingUsers.size());

        if (waitingUsers.size() >= 2) {
            String user1 = waitingUsers.poll();
            String user2 = waitingUsers.poll();
            log.info("Match created between {} and {}", user1, user2);

            MatchFoundEvent matchEvent = new MatchFoundEvent(
                    UserEventType.MATCH_FOUND.name(),
                    List.of(user1, user2),
                    Instant.now()
            );

            MatchFoundPublisher publisher = matchFoundPublisher.getIfAvailable();
            if (publisher == null) {
                log.warn("Redis publishing is disabled; MATCH_FOUND event for {} and {} was not published.",
                        user1, user2);
                return Mono.empty();
            }

            return publisher.publishMatch(matchEvent);
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
