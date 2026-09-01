package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.document.MatchFoundEvent;
import org.ttarena.matchmaking.document.MatchFoundEvent.Participant;
import org.ttarena.matchmaking.util.UserEventType;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class MatchmakingService {

    private static final int REMEMBERED_MATCHES = 1000;

    private final Queue<Participant> waitingPlayers = new ConcurrentLinkedQueue<>();

    private final Map<String, MatchFoundEvent> recentMatches = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, MatchFoundEvent> eldest) {
                    return size() > REMEMBERED_MATCHES;
                }
            });

    private final ObjectProvider<MatchFoundPublisher> matchFoundPublisher;

    public MatchmakingService(ObjectProvider<MatchFoundPublisher> matchFoundPublisher) {
        this.matchFoundPublisher = matchFoundPublisher;
    }

    public Mono<Void> enqueueUser(String userId, String characterId) {
        if (userId == null || userId.isBlank() || characterId == null || characterId.isBlank()) {
            log.warn("Ignoring queue request with no user or character id.");
            return Mono.empty();
        }

        if (isQueued(userId)) {
            log.debug("User {} is already queued.", userId);
            return Mono.empty();
        }

        waitingPlayers.add(new Participant(userId, characterId));
        log.info("User {} queued with character {}. Queue size: {}", userId, characterId, waitingPlayers.size());

        if (waitingPlayers.size() < 2) {
            return Mono.empty();
        }

        Participant first = waitingPlayers.poll();
        Participant second = waitingPlayers.poll();
        if (first == null || second == null) {
            if (first != null) {
                waitingPlayers.add(first);
            }
            return Mono.empty();
        }

        log.info("Match created between {} and {}", first.getUserId(), second.getUserId());
        MatchFoundEvent matchEvent = new MatchFoundEvent(
                UserEventType.MATCH_FOUND.name(),
                List.of(first, second),
                Instant.now());

        recentMatches.put(first.getUserId(), matchEvent);
        recentMatches.put(second.getUserId(), matchEvent);

        MatchFoundPublisher publisher = matchFoundPublisher.getIfAvailable();
        if (publisher == null) {
            log.warn("Redis publishing is disabled; MATCH_FOUND for {} and {} was not published.",
                    first.getUserId(), second.getUserId());
            return Mono.empty();
        }

        return publisher.publishMatch(matchEvent);
    }

    public Mono<Void> dequeueUser(String userId) {
        boolean removed = waitingPlayers.removeIf(player -> player.getUserId().equals(userId));
        if (removed) {
            log.info("User {} removed from matchmaking queue.", userId);
        } else {
            log.warn("Attempted to remove user {} who was not in the queue.", userId);
        }
        return Mono.empty();
    }

    public boolean isQueued(String userId) {
        return waitingPlayers.stream().anyMatch(player -> player.getUserId().equals(userId));
    }

    public int queueSize() {
        return waitingPlayers.size();
    }

    public Optional<MatchFoundEvent> lastMatchOf(String userId) {
        return Optional.ofNullable(recentMatches.get(userId));
    }
}
