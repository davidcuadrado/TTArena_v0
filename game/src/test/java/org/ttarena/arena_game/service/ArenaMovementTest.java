package org.ttarena.arena_game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ttarena.arena_game.client.CharacterServiceClient;
import org.ttarena.arena_game.client.CombatResultResponse;
import org.ttarena.arena_game.client.MapServiceClient;
import org.ttarena.arena_game.client.PathResponse;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import org.ttarena.arena_game.document.HexCoordinate;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.repository.GameSessionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArenaMovementTest {

    private static final String ALICE = "user-alice";
    private static final String BOB = "user-bob";
    private static final String ALICE_CHARACTER = "char-alice";
    private static final String BOB_CHARACTER = "char-bob";
    private static final String TOKEN = "Bearer token";
    private static final String ARENA = "arena-1";
    private static final Instant NOW = Instant.parse("2026-09-01T10:00:00Z");

    private static final HexCoordinate WEST = new HexCoordinate(-3, 0, 3);
    private static final HexCoordinate EAST = new HexCoordinate(3, 0, -3);

    private GameSessionRepository repository;
    private CharacterServiceClient characterService;
    private MapServiceClient mapService;
    private GameSessionService gameSessions;

    @BeforeEach
    void setUp() {
        repository = mock(GameSessionRepository.class);
        characterService = mock(CharacterServiceClient.class);
        mapService = mock(MapServiceClient.class);
        gameSessions = new GameSessionService(repository, characterService, mapService,
                Clock.fixed(NOW, ZoneOffset.UTC), 120, ARENA, 4);
        when(repository.save(any(GameSession.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private GameSession session(boolean deployed) {
        GameSession.Participant alice = new GameSession.Participant(ALICE, ALICE_CHARACTER);
        GameSession.Participant bob = new GameSession.Participant(BOB, BOB_CHARACTER);
        if (deployed) {
            alice.setPosition(WEST);
            alice.setMovementRemaining(4);
            bob.setPosition(EAST);
            bob.setMovementRemaining(4);
        }
        GameSession session = GameSession.builder()
                .id("game-1")
                .participants(List.of(alice, bob))
                .currentTurnUserId(ALICE)
                .status(GameStatus.IN_PROGRESS)
                .turnNumber(1)
                .arenaMapId(ARENA)
                .turnDeadline(NOW.plusSeconds(120))
                .createdAt(NOW)
                .turns(new java.util.ArrayList<>())
                .build();
        when(repository.findById("game-1")).thenReturn(Mono.just(session));
        return session;
    }

    @Test
    void bothPlayersAreDeployedOnTheFirstActionBecauseMatchFoundCarriesNoToken() {
        session(false);
        when(mapService.deployments(TOKEN, ARENA, 2)).thenReturn(Mono.just(List.of(WEST, EAST)));
        when(mapService.path(eq(TOKEN), eq(ARENA), any(), any()))
                .thenReturn(Mono.just(new PathResponse(List.of(WEST, new HexCoordinate(-2, 0, 2)), 1, true)));

        GameSession moved = gameSessions.move("game-1", ALICE, TOKEN, new HexCoordinate(-2, 0, 2)).block();

        assertThat(moved).isNotNull();
        assertThat(moved.participantOf(BOB).getPosition()).isEqualTo(EAST);
        verify(mapService).deployments(TOKEN, ARENA, 2);
    }

    @Test
    void deploymentHappensOnlyOnce() {
        session(true);
        when(mapService.path(eq(TOKEN), eq(ARENA), any(), any()))
                .thenReturn(Mono.just(new PathResponse(List.of(WEST, new HexCoordinate(-2, 0, 2)), 1, true)));

        gameSessions.move("game-1", ALICE, TOKEN, new HexCoordinate(-2, 0, 2)).block();

        verify(mapService, never()).deployments(anyString(), anyString(), anyInt());
    }

    @Test
    void movingChargesThePathCostAgainstThisTurnsBudget() {
        session(true);
        HexCoordinate destination = new HexCoordinate(-1, 0, 1);
        when(mapService.path(TOKEN, ARENA, WEST, destination))
                .thenReturn(Mono.just(new PathResponse(List.of(WEST, destination), 3, true)));

        GameSession moved = gameSessions.move("game-1", ALICE, TOKEN, destination).block();

        assertThat(moved).isNotNull();
        assertThat(moved.participantOf(ALICE).getPosition()).isEqualTo(destination);
        assertThat(moved.participantOf(ALICE).getMovementRemaining()).isEqualTo(1);
    }

    @Test
    void aMoveCostingMoreThanTheBudgetIsRefused() {
        session(true);
        HexCoordinate destination = new HexCoordinate(0, 0, 0);
        when(mapService.path(TOKEN, ARENA, WEST, destination))
                .thenReturn(Mono.just(new PathResponse(List.of(WEST, destination), 9, true)));

        StepVerifier.create(gameSessions.move("game-1", ALICE, TOKEN, destination))
                .expectError(BadRequestException.class)
                .verify();

        verify(repository, never()).save(any(GameSession.class));
    }

    @Test
    void anUnreachableDestinationIsRefused() {
        session(true);
        HexCoordinate destination = new HexCoordinate(0, 0, 0);
        when(mapService.path(TOKEN, ARENA, WEST, destination))
                .thenReturn(Mono.just(new PathResponse(List.of(), 0, false)));

        StepVerifier.create(gameSessions.move("game-1", ALICE, TOKEN, destination))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void youCannotWalkOntoYourOpponent() {
        session(true);

        StepVerifier.create(gameSessions.move("game-1", ALICE, TOKEN, EAST))
                .expectError(BadRequestException.class)
                .verify();

        verify(mapService, never()).path(anyString(), anyString(), any(), any());
    }

    @Test
    void movingOutOfTurnIsRefused() {
        session(true);

        StepVerifier.create(gameSessions.move("game-1", BOB, TOKEN, new HexCoordinate(2, 0, -2)))
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    void castReportsTheDistanceBetweenTheTwoPlayers() {
        session(true);
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER), 6))
                .thenReturn(Mono.just(hit()));

        gameSessions.cast("game-1", ALICE, TOKEN, "ability-1").block();

        verify(characterService).cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER), 6);
    }

    @Test
    void theIncomingPlayerStartsTheirTurnWithAFullMovementBudget() {
        GameSession session = session(true);
        session.participantOf(BOB).setMovementRemaining(0);
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER), 6))
                .thenReturn(Mono.just(hit()));

        GameSession after = gameSessions.cast("game-1", ALICE, TOKEN, "ability-1").block();

        assertThat(after).isNotNull();
        assertThat(after.getCurrentTurnUserId()).isEqualTo(BOB);
        assertThat(after.participantOf(BOB).getMovementRemaining()).isEqualTo(4);
    }

    @Test
    void withoutAnArenaThereIsNoDistanceAndNoMoving() {
        GameSessionService boardless = new GameSessionService(repository, characterService, mapService,
                Clock.fixed(NOW, ZoneOffset.UTC), 120, null, 4);
        GameSession session = session(false);
        session.setArenaMapId(null);
        when(characterService.cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER), null))
                .thenReturn(Mono.just(hit()));

        boardless.cast("game-1", ALICE, TOKEN, "ability-1").block();

        verify(characterService).cast(TOKEN, ALICE_CHARACTER, "ability-1", List.of(BOB_CHARACTER), null);
        verify(mapService, never()).deployments(anyString(), anyString(), anyInt());
    }

    private CombatResultResponse hit() {
        return new CombatResultResponse("caster", "Caster", "ability-1", "Lightning Bolt", "DAMAGE", 25, 70,
                List.of(new CombatResultResponse.TargetOutcomeResponse(BOB_CHARACTER, "Bob", 20, 80, false)));
    }
}
