package org.ttarena.arena_game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ttarena.arena_game.client.CharacterServiceClient;
import org.ttarena.arena_game.client.CombatResultResponse;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.repository.GameSessionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
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

    private GameSessionService gameSessions;

    @BeforeEach
    void setUp() {
        gameSessions = new GameSessionService(repository, characterService);
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
                .createdAt(Instant.now())
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
}
