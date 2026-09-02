package org.ttarena.matchmaking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.ttarena.matchmaking.document.MatchFoundEvent;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchmakingServiceTest {

    private static final long QUEUE_TTL_SECONDS = 120;

    private final List<MatchFoundEvent> published = new ArrayList<>();
    private MatchmakingService matchmaking;
    private MutableClock clock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        published.clear();

        MatchFoundPublisher publisher = mock(MatchFoundPublisher.class);
        when(publisher.publishMatch(any(MatchFoundEvent.class))).thenAnswer(invocation -> {
            published.add(invocation.getArgument(0));
            return Mono.empty();
        });

        ObjectProvider<MatchFoundPublisher> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(publisher);

        clock = new MutableClock(Instant.parse("2026-09-01T10:00:00Z"));
        matchmaking = new MatchmakingService(provider, clock, QUEUE_TTL_SECONDS);
    }

    @Test
    void oneWaitingPlayerIsNotAMatch() {
        matchmaking.enqueueUser("alice", "char-alice").block();

        assertThat(published).isEmpty();
        assertThat(matchmaking.isQueued("alice")).isTrue();
        assertThat(matchmaking.queueSize()).isEqualTo(1);
        assertThat(matchmaking.lastMatchOf("alice")).isEmpty();
    }

    @Test
    void theSecondPlayerCompletesTheMatchAndEmptiesTheQueue() {
        matchmaking.enqueueUser("alice", "char-alice").block();
        matchmaking.enqueueUser("bob", "char-bob").block();

        assertThat(published).hasSize(1);
        assertThat(published.get(0).getParticipants()).extracting(participant -> participant.getUserId())
                .containsExactly("alice", "bob");
        assertThat(published.get(0).getParticipants()).extracting(participant -> participant.getCharacterId())
                .containsExactly("char-alice", "char-bob");

        assertThat(matchmaking.queueSize()).isZero();
        assertThat(matchmaking.isQueued("alice")).isFalse();
        assertThat(matchmaking.isQueued("bob")).isFalse();
    }

    @Test
    void bothPlayersCanLookTheirMatchUp() {
        matchmaking.enqueueUser("alice", "char-alice").block();
        matchmaking.enqueueUser("bob", "char-bob").block();

        assertThat(matchmaking.lastMatchOf("alice")).isPresent();
        assertThat(matchmaking.lastMatchOf("bob")).isPresent();
        assertThat(matchmaking.lastMatchOf("alice").orElseThrow())
                .isSameAs(matchmaking.lastMatchOf("bob").orElseThrow());
    }

    /**
     * Two join calls from the same client must not pair a player with themselves.
     */
    @Test
    void joiningTwiceDoesNotMatchAPlayerWithThemselves() {
        matchmaking.enqueueUser("alice", "char-alice").block();
        matchmaking.enqueueUser("alice", "char-alice").block();

        assertThat(published).isEmpty();
        assertThat(matchmaking.queueSize()).isEqualTo(1);
    }

    @Test
    void leavingTheQueueRemovesTheWaitingPlayer() {
        matchmaking.enqueueUser("alice", "char-alice").block();
        matchmaking.dequeueUser("alice").block();

        assertThat(matchmaking.isQueued("alice")).isFalse();
        assertThat(matchmaking.queueSize()).isZero();

        matchmaking.enqueueUser("bob", "char-bob").block();
        assertThat(published).isEmpty();
    }

    @Test
    void queueingWithoutACharacterIsIgnored() {
        matchmaking.enqueueUser("alice", null).block();
        matchmaking.enqueueUser("alice", "  ").block();

        assertThat(matchmaking.queueSize()).isZero();
    }

    /**
     * A player who closes the tab never sends USER_DISCONNECTED. Their entry has
     * to age out, or the next player is matched against a ghost.
     */
    @Test
    void staleEntriesAreDroppedRatherThanMatched() {
        matchmaking.enqueueUser("alice", "char-alice").block();

        clock.advance(Duration.ofSeconds(QUEUE_TTL_SECONDS + 1));

        matchmaking.enqueueUser("bob", "char-bob").block();

        assertThat(published).isEmpty();
        assertThat(matchmaking.isQueued("alice")).isFalse();
        assertThat(matchmaking.isQueued("bob")).isTrue();
        assertThat(matchmaking.queueSize()).isEqualTo(1);
    }

    @Test
    void anEntryWithinTheTtlStillMatches() {
        matchmaking.enqueueUser("alice", "char-alice").block();

        clock.advance(Duration.ofSeconds(QUEUE_TTL_SECONDS - 1));

        matchmaking.enqueueUser("bob", "char-bob").block();

        assertThat(published).hasSize(1);
    }

    @Test
    void aBlankUserIdIsIgnored() {
        matchmaking.enqueueUser(null, "char-x").block();
        matchmaking.enqueueUser("  ", "char-x").block();

        assertThat(matchmaking.queueSize()).isZero();
    }

    @Test
    void fourPlayersMakeTwoMatches() {
        List.of("alice", "bob", "carol", "dave")
                .forEach(player -> matchmaking.enqueueUser(player, "char-" + player).block());

        assertThat(published).hasSize(2);
        assertThat(published.get(0).getParticipants()).extracting(participant -> participant.getUserId())
                .containsExactly("alice", "bob");
        assertThat(published.get(1).getParticipants()).extracting(participant -> participant.getUserId())
                .containsExactly("carol", "dave");
        assertThat(matchmaking.queueSize()).isZero();
    }

    /** A clock the test can move forward, so nothing has to sleep. */
    private static final class MutableClock extends Clock {

        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
