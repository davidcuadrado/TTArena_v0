package org.ttarena.matchmaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.document.MatchFoundEvent;
import org.ttarena.matchmaking.document.MatchFoundEvent.Participant;
import org.ttarena.matchmaking.util.UserEventType;

import java.time.Clock;
import java.time.Duration;
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

    private final Queue<WaitingPlayer> waitingPlayers = new ConcurrentLinkedQueue<>();

    private final Map<String, MatchFoundEvent> recentMatches = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, MatchFoundEvent> eldest) {
                    return size() > REMEMBERED_MATCHES;
                }
            });

    private final ObjectProvider<MatchFoundPublisher> matchFoundPublisher;
    private final Clock clock;
    private final Duration entryTtl;

    public MatchmakingService(ObjectProvider<MatchFoundPublisher> matchFoundPublisher,
                              Clock clock,
                              @Value("${matchmaking.queue.entry-ttl-seconds:120}") long entryTtlSeconds) {
        this.matchFoundPublisher = matchFoundPublisher;
        this.clock = clock;
        this.entryTtl = Duration.ofSeconds(entryTtlSeconds);
    }

    public Mono<Void> enqueueUser(String userId, String characterId) {
        if (userId == null || userId.isBlank() || characterId == null || characterId.isBlank()) {
            log.warn("Ignoring queue request with no user or character id.");
            return Mono.empty();
        }

        purgeExpired();

        if (isQueued(userId)) {
            log.debug("User {} is already queued.", userId);
            return Mono.empty();
        }

        waitingPlayers.add(new WaitingPlayer(new Participant(userId, characterId), Instant.now(clock)));
        log.info("User {} queued with character {}. Queue size: {}", userId, characterId, waitingPlayers.size());

        if (waitingPlayers.size() < 2) {
            return Mono.empty();
        }

        WaitingPlayer first = waitingPlayers.poll();
        WaitingPlayer second = waitingPlayers.poll();
        if (first == null || second == null) {
            if (first != null) {
                waitingPlayers.add(first);
            }
            return Mono.empty();
        }

        log.info("Match created between {} and {}", first.participant().getUserId(), second.participant().getUserId());
        MatchFoundEvent matchEvent = new MatchFoundEvent(
                UserEventType.MATCH_FOUND.name(),
                List.of(first.participant(), second.participant()),
                Instant.now(clock));

        recentMatches.put(first.participant().getUserId(), matchEvent);
        recentMatches.put(second.participant().getUserId(), matchEvent);

        MatchFoundPublisher publisher = matchFoundPublisher.getIfAvailable();
        if (publisher == null) {
            log.warn("Redis publishing is disabled; MATCH_FOUND for {} and {} was not published.",
                    first.participant().getUserId(), second.participant().getUserId());
            return Mono.empty();
        }

        return publisher.publishMatch(matchEvent);
    }

    public Mono<Void> dequeueUser(String userId) {
        boolean removed = waitingPlayers.removeIf(player -> player.participant().getUserId().equals(userId));
        if (removed) {
            log.info("User {} removed from matchmaking queue.", userId);
        } else {
            log.warn("Attempted to remove user {} who was not in the queue.", userId);
        }
        return Mono.empty();
    }

    /**
     * Drops entries older than the TTL. A player who closes the tab never sends
     * USER_DISCONNECTED, and pairing someone against a ghost wastes the match.
     */
    private void purgeExpired() {
        Instant cutoff = Instant.now(clock).minus(entryTtl);
        waitingPlayers.removeIf(player -> {
            boolean expired = player.queuedAt().isBefore(cutoff);
            if (expired) {
                log.info("Dropping stale queue entry for {} (waiting since {}).",
                        player.participant().getUserId(), player.queuedAt());
            }
            return expired;
        });
    }

    public boolean isQueued(String userId) {
        return waitingPlayers.stream().anyMatch(player -> player.participant().getUserId().equals(userId));
    }

    public int queueSize() {
        purgeExpired();
        return waitingPlayers.size();
    }

    public Optional<MatchFoundEvent> lastMatchOf(String userId) {
        return Optional.ofNullable(recentMatches.get(userId));
    }

    private record WaitingPlayer(Participant participant, Instant queuedAt) {
    }
}
