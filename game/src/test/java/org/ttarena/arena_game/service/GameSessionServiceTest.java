package org.ttarena.arena_game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ttarena.arena_game.client.CharacterServiceClient;
import org.ttarena.arena_game.client.CombatResultResponse;
import org.ttarena.arena_game.document.EndReason;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.repository.GameSessionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    private static final String ALICE = "user-alice";
    private static final String BOB = "user-bob";
    private static final String ALICE_CHARACTER = "char-alice";
    private static final String BOB_CHARACTER = "char-bob";
    private static final String TOKEN = "Bearer token";

    @Mock
    private GameSessionRepository repository;

    @Mock
    private CharacterServiceClient characterService;

    private static final long TURN_TIMEOUT_SECONDS = 120;

    private GameSessionService gameSessions;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-09-01T10:00:00Z"));
        gameSessions = new GameSessionService(repository, characterService, clock, TURN_TIMEOUT_SECONDS);
    }

    private GameSession inProgress() {
        return GameSession.builder()
                .id("game-1")
                .participants(List.of(
                        new GameSession.Participant(ALICE, ALICE_CHARACTER),
                        new GameSession.Participant(BOB, BOB_CHARACTER)))
                .currentTurnUserId(ALICE)
                .status(GameStatus.IN_PROGRESS)
                .turnNumber(1)
                .createdAt(Instant.now(clock))
                .turnDeadline(Instant.now(clock).plusSeconds(TURN_TIMEOUT_SECONDS))
                .turns(new ArrayList<>())
                .build();
    }

    private CombatResultResponse hitFor(int amount, int remainingHealth, boolean defeated) {
        return new CombatResultResponse("caster", "Caster", "ability-1", "Mortal Strike", "DAMAGE", 30, 70,
                List.of(new CombatResultResponse.TargetOutcomeResponse(
                        BOB_CHARACTER, "Bob", amount, remainingHealth, defeated)));
    }

    private void stubSave() {
        when(repository.save(any(GameSession.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void startingASessionPutsTheFirstQueuedPlayerOnTurn() {
        stubSave();

        GameSession session = gameSessions.startSession(List.of(
                new GameSession.Participant(ALICE, ALICE_CHARACTER),
                new GameSession.Participant(BOB, BOB_CHARACTER))).block();

        assertThat(session).isNotNull();
        assertThat(session.getCurrentTurnUserId()).isEqualTo(ALICE);
        assertThat(session.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(session.getTurnNumber()).isEqualTo(1);
    }

    @Test
    void aSessionNeedsExactlyTwoParticipants() {
        StepVerifier.create(gameSessions.startSession(
                        List.of(new GameSession.Participant(ALICE, ALICE_CHARACTER))))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void castingOnYourTurnHitsTheOpponentAndPassesTheTurn() {
        GameSession session = inProgress();
        when(repository.findById("game-1")).thenReturn(Mono.just(session));
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER)))
                .thenReturn(Mono.just(hitFor(89, 111, false)));
        stubSave();

        GameSession updated = gameSessions.cast("game-1", ALICE, TOKEN, "ability-1").block();

        assertThat(updated).isNotNull();
        assertThat(updated.getCurrentTurnUserId()).isEqualTo(BOB);
        assertThat(updated.getTurnNumber()).isEqualTo(2);
        assertThat(updated.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(updated.getTurns()).hasSize(1);
        assertThat(updated.getTurns().get(0).getAmount()).isEqualTo(89);
        assertThat(updated.getTurns().get(0).getUserId()).isEqualTo(ALICE);
    }

    /**
     * The turn is this module's rule, so it has to be refused here - the
     * character service has no idea whose turn it is.
     */
    @Test
    void castingOutOfTurnIsRefusedWithoutCallingTheCharacterService() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        StepVerifier.create(gameSessions.cast("game-1", BOB, TOKEN, "ability-1"))
                .expectError(ForbiddenException.class)
                .verify();

        verify(characterService, never()).cast(anyString(), anyString(), anyString(), anyList());
        verify(repository, never()).save(any(GameSession.class));
    }

    @Test
    void someoneElsesGameIsNotVisible() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        StepVerifier.create(gameSessions.cast("game-1", "user-carol", TOKEN, "ability-1"))
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    void defeatingTheOpponentFinishesTheGame() {
        GameSession session = inProgress();
        when(repository.findById("game-1")).thenReturn(Mono.just(session));
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER)))
                .thenReturn(Mono.just(hitFor(200, 0, true)));
        stubSave();

        GameSession finished = gameSessions.cast("game-1", ALICE, TOKEN, "ability-1").block();

        assertThat(finished).isNotNull();
        assertThat(finished.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(finished.getWinnerUserId()).isEqualTo(ALICE);
        assertThat(finished.getFinishedAt()).isNotNull();
        assertThat(finished.getCurrentTurnUserId()).isNull();
    }

    @Test
    void aFinishedGameTakesNoMoreTurns() {
        GameSession session = inProgress();
        session.setStatus(GameStatus.FINISHED);
        session.setWinnerUserId(ALICE);
        when(repository.findById("game-1")).thenReturn(Mono.just(session));

        StepVerifier.create(gameSessions.cast("game-1", ALICE, TOKEN, "ability-1"))
                .expectError(BadRequestException.class)
                .verify();

        verify(characterService, never()).cast(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    void anUnknownGameIsNotFound() {
        when(repository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(gameSessions.cast("nope", ALICE, TOKEN, "ability-1"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void everyTurnGetsAFreshDeadline() {
        GameSession session = inProgress();
        when(repository.findById("game-1")).thenReturn(Mono.just(session));
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER)))
                .thenReturn(Mono.just(hitFor(89, 111, false)));
        stubSave();

        clock.advance(Duration.ofSeconds(30));
        GameSession updated = gameSessions.cast("game-1", ALICE, TOKEN, "ability-1").block();

        assertThat(updated).isNotNull();
        assertThat(updated.getTurnDeadline())
                .isEqualTo(Instant.parse("2026-09-01T10:00:30Z").plusSeconds(TURN_TIMEOUT_SECONDS));
    }

    @Test
    void aTurnPlayedAfterItsDeadlineIsRefused() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        clock.advance(Duration.ofSeconds(TURN_TIMEOUT_SECONDS + 1));

        StepVerifier.create(gameSessions.cast("game-1", ALICE, TOKEN, "ability-1"))
                .expectError(BadRequestException.class)
                .verify();

        verify(characterService, never()).cast(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    void theWaitingPlayerCanClaimTheWinOnceTheDeadlinePasses() {
        GameSession session = inProgress();
        when(repository.findById("game-1")).thenReturn(Mono.just(session));
        stubSave();

        clock.advance(Duration.ofSeconds(TURN_TIMEOUT_SECONDS + 1));

        GameSession finished = gameSessions.claimTimeoutWin("game-1", BOB).block();

        assertThat(finished).isNotNull();
        assertThat(finished.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(finished.getWinnerUserId()).isEqualTo(BOB);
        assertThat(finished.getEndReason()).isEqualTo(EndReason.TIMEOUT);
    }

    @Test
    void aTimeoutCannotBeClaimedEarly() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        clock.advance(Duration.ofSeconds(TURN_TIMEOUT_SECONDS - 1));

        StepVerifier.create(gameSessions.claimTimeoutWin("game-1", BOB))
                .expectError(BadRequestException.class)
                .verify();

        verify(repository, never()).save(any(GameSession.class));
    }

    @Test
    void youCannotClaimATimeoutAgainstYourself() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        clock.advance(Duration.ofSeconds(TURN_TIMEOUT_SECONDS + 1));

        StepVerifier.create(gameSessions.claimTimeoutWin("game-1", ALICE))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void surrenderingHandsTheWinToTheOpponent() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));
        stubSave();

        GameSession finished = gameSessions.surrender("game-1", ALICE).block();

        assertThat(finished).isNotNull();
        assertThat(finished.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(finished.getWinnerUserId()).isEqualTo(BOB);
        assertThat(finished.getEndReason()).isEqualTo(EndReason.SURRENDER);
        assertThat(finished.getCurrentTurnUserId()).isNull();
    }

    @Test
    void youCanSurrenderOutOfTurn() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));
        stubSave();

        GameSession finished = gameSessions.surrender("game-1", BOB).block();

        assertThat(finished.getWinnerUserId()).isEqualTo(ALICE);
    }

    @Test
    void aRematchStartsAFreshGameWithTheLoserMovingFirst() {
        GameSession finished = inProgress();
        finished.setStatus(GameStatus.FINISHED);
        finished.setWinnerUserId(ALICE);
        finished.setEndReason(EndReason.DEFEAT);

        when(repository.findById("game-1")).thenReturn(Mono.just(finished));
        when(repository.findByRematchOfSessionId("game-1")).thenReturn(Mono.empty());
        stubSave();

        GameSession rematch = gameSessions.rematch("game-1", ALICE).block();

        assertThat(rematch).isNotNull();
        assertThat(rematch.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(rematch.getCurrentTurnUserId()).isEqualTo(BOB);
        assertThat(rematch.getRematchOfSessionId()).isEqualTo("game-1");
        assertThat(rematch.getTurns()).isEmpty();
    }

    @Test
    void agameStillRunningCannotBeRematched() {
        when(repository.findById("game-1")).thenReturn(Mono.just(inProgress()));

        StepVerifier.create(gameSessions.rematch("game-1", ALICE))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void onlyOneRematchPerGame() {
        GameSession finished = inProgress();
        finished.setStatus(GameStatus.FINISHED);
        finished.setWinnerUserId(ALICE);

        GameSession existing = inProgress();
        existing.setId("game-2");

        when(repository.findById("game-1")).thenReturn(Mono.just(finished));
        when(repository.findByRematchOfSessionId("game-1")).thenReturn(Mono.just(existing));

        StepVerifier.create(gameSessions.rematch("game-1", ALICE))
                .expectError(BadRequestException.class)
                .verify();
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
