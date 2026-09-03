package org.ttarena.arena_game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ttarena.arena_game.client.CharacterServiceClient;
import org.ttarena.arena_game.client.CombatResultResponse;
import org.ttarena.arena_game.client.MapServiceClient;
import org.ttarena.arena_game.document.EndReason;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import org.ttarena.arena_game.document.HexCoordinate;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.repository.GameSessionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GameSessionService {

    private final GameSessionRepository sessions;
    private final CharacterServiceClient characterService;
    private final MapServiceClient mapService;
    private final Clock clock;
    private final Duration turnTimeout;
    private final String arenaMapId;
    private final int movementPerTurn;

    public GameSessionService(GameSessionRepository sessions,
                              CharacterServiceClient characterService,
                              MapServiceClient mapService,
                              Clock clock,
                              @Value("${game.turn.timeout-seconds:120}") long turnTimeoutSeconds,
                              @Value("${game.arena.map-id:}") String arenaMapId,
                              @Value("${game.arena.movement-per-turn:4}") int movementPerTurn) {
        this.sessions = sessions;
        this.characterService = characterService;
        this.mapService = mapService;
        this.clock = clock;
        this.turnTimeout = Duration.ofSeconds(turnTimeoutSeconds);
        this.arenaMapId = arenaMapId == null || arenaMapId.isBlank() ? null : arenaMapId;
        this.movementPerTurn = movementPerTurn;
    }

    public Mono<GameSession> startSession(List<GameSession.Participant> participants) {
        return startSession(participants, participants == null || participants.isEmpty()
                ? null
                : participants.get(0).getUserId(), null);
    }

    private Mono<GameSession> startSession(List<GameSession.Participant> participants, String firstTurnUserId,
                                           String rematchOfSessionId) {
        if (participants == null || participants.size() != 2) {
            return Mono.error(new BadRequestException("A session needs exactly two participants."));
        }

        List<GameSession.Participant> fresh = participants.stream()
                .map(participant -> new GameSession.Participant(
                        participant.getUserId(), participant.getCharacterId()))
                .toList();
        fresh.forEach(participant -> participant.setMovementRemaining(movementPerTurn));

        Instant now = Instant.now(clock);
        GameSession session = GameSession.builder()
                .participants(fresh)
                .arenaMapId(arenaMapId)
                .currentTurnUserId(firstTurnUserId)
                .status(GameStatus.IN_PROGRESS)
                .turnNumber(1)
                .turnDeadline(now.plus(turnTimeout))
                .createdAt(now)
                .rematchOfSessionId(rematchOfSessionId)
                .turns(new ArrayList<>())
                .build();

        return sessions.save(session)
                .doOnNext(saved -> log.info("Session {} started: {} vs {}", saved.getId(),
                        participants.get(0).getUserId(), participants.get(1).getUserId()));
    }

    public Flux<GameSession> sessionsOf(String userId) {
        return sessions.findByParticipantsUserId(userId);
    }

    public Mono<GameSession> activeSessionOf(String userId) {
        return sessions.findFirstByParticipantsUserIdAndStatusOrderByCreatedAtDesc(userId, GameStatus.IN_PROGRESS)
                .switchIfEmpty(Mono.error(new NotFoundException("You have no game in progress.")));
    }

    public Mono<GameSession> sessionFor(String sessionId, String userId) {
        return sessions.findById(sessionId)
                .switchIfEmpty(Mono.error(new NotFoundException("No game with id: " + sessionId)))
                .flatMap(session -> session.participantOf(userId) == null
                        ? Mono.error(new ForbiddenException("You are not a player in this game."))
                        : Mono.just(session));
    }

    public Mono<GameSession> cast(String sessionId, String userId, String bearerToken, String abilityId) {
        return activeSessionFor(sessionId, userId)
                .flatMap(session -> ensureDeployed(session, bearerToken))
                .flatMap(session -> {
            if (!userId.equals(session.getCurrentTurnUserId())) {
                return Mono.error(new ForbiddenException("It is not your turn."));
            }
            if (turnHasExpired(session)) {
                return Mono.error(new BadRequestException(
                        "Your turn ran out; your opponent can claim the win."));
            }

            GameSession.Participant you = session.participantOf(userId);
            GameSession.Participant opponent = session.opponentOf(userId);
            if (opponent == null) {
                return Mono.error(new BadRequestException("This game has no opponent."));
            }

            return characterService
                    .cast(bearerToken, you.getCharacterId(), abilityId, List.of(opponent.getCharacterId()),
                            separation(you, opponent))
                    .map(result -> applyResult(session, userId, opponent, result))
                    .flatMap(sessions::save);
        });
    }

    /**
     * Walks to a tile, paying its path cost out of this turn's movement. Moving
     * does not end the turn: you may move and then cast.
     */
    public Mono<GameSession> move(String sessionId, String userId, String bearerToken, HexCoordinate destination) {
        return activeSessionFor(sessionId, userId)
                .flatMap(session -> ensureDeployed(session, bearerToken))
                .flatMap(session -> {
                    if (session.getArenaMapId() == null) {
                        return Mono.error(new BadRequestException("This game is not being played on an arena."));
                    }
                    if (!userId.equals(session.getCurrentTurnUserId())) {
                        return Mono.error(new ForbiddenException("It is not your turn."));
                    }
                    if (turnHasExpired(session)) {
                        return Mono.error(new BadRequestException(
                                "Your turn ran out; your opponent can claim the win."));
                    }

                    GameSession.Participant you = session.participantOf(userId);
                    GameSession.Participant opponent = session.opponentOf(userId);

                    if (destination.equals(you.getPosition())) {
                        return Mono.error(new BadRequestException("You are already standing there."));
                    }
                    if (opponent != null && destination.equals(opponent.getPosition())) {
                        return Mono.error(new BadRequestException("Your opponent is standing there."));
                    }

                    return mapService.path(bearerToken, session.getArenaMapId(), you.getPosition(), destination)
                            .flatMap(route -> {
                                if (!route.reachable()) {
                                    return Mono.error(new BadRequestException(
                                            "There is no route to " + destination.key() + "."));
                                }
                                if (route.movementCost() > you.getMovementRemaining()) {
                                    return Mono.error(new BadRequestException(
                                            "That move costs %d and you have %d movement left this turn."
                                                    .formatted(route.movementCost(), you.getMovementRemaining())));
                                }

                                you.setMovementRemaining(you.getMovementRemaining() - route.movementCost());
                                you.setPosition(destination);
                                return sessions.save(session);
                            });
                });
    }

    /**
     * Places both players on the arena the first time either of them acts.
     * A match.found event carries no user token, so this cannot happen when the
     * session is created - it happens on the first request that has one.
     */
    private Mono<GameSession> ensureDeployed(GameSession session, String bearerToken) {
        if (session.getArenaMapId() == null || alreadyDeployed(session)) {
            return Mono.just(session);
        }

        int playerCount = session.getParticipants().size();
        return mapService.startingPositions(bearerToken, session.getArenaMapId(), playerCount)
                .flatMap(startingPositions -> {
                    if (startingPositions.size() < playerCount) {
                        return Mono.error(new BadRequestException(
                                "Arena " + session.getArenaMapId() + " has no room for both players."));
                    }
                    for (int i = 0; i < playerCount; i++) {
                        GameSession.Participant participant = session.getParticipants().get(i);
                        participant.setPosition(startingPositions.get(i));
                        participant.setMovementRemaining(movementPerTurn);
                    }
                    log.info("Session {} deployed on arena {}", session.getId(), session.getArenaMapId());
                    return sessions.save(session);
                });
    }

    private static boolean alreadyDeployed(GameSession session) {
        return session.getParticipants().stream().allMatch(participant -> participant.getPosition() != null);
    }

    private static Integer separation(GameSession.Participant you, GameSession.Participant opponent) {
        return you.getPosition() == null || opponent.getPosition() == null
                ? null
                : you.getPosition().distanceTo(opponent.getPosition());
    }

    /**
     * Gives up the game. The opponent wins immediately.
     */
    public Mono<GameSession> surrender(String sessionId, String userId) {
        return activeSessionFor(sessionId, userId).flatMap(session -> {
            GameSession.Participant opponent = session.opponentOf(userId);
            finish(session, opponent == null ? null : opponent.getUserId(), EndReason.SURRENDER);
            log.info("Session {} surrendered by {}", session.getId(), userId);
            return sessions.save(session);
        });
    }

    /**
     * Claims the win when the opponent let their turn run out. Deliberately a
     * request rather than a background sweep: nothing has to be scheduled, and a
     * game only ends when someone is actually waiting on it.
     */
    public Mono<GameSession> claimTimeoutWin(String sessionId, String userId) {
        return activeSessionFor(sessionId, userId).flatMap(session -> {
            if (userId.equals(session.getCurrentTurnUserId())) {
                return Mono.error(new BadRequestException("It is your own turn; play it or surrender."));
            }
            if (!turnHasExpired(session)) {
                return Mono.error(new BadRequestException(
                        "Your opponent still has until " + session.getTurnDeadline() + "."));
            }

            finish(session, userId, EndReason.TIMEOUT);
            log.info("Session {} awarded to {} on timeout", session.getId(), userId);
            return sessions.save(session);
        });
    }

    /**
     * Starts a fresh game between the same two players, with the loser moving
     * first. Only one rematch per finished session.
     */
    public Mono<GameSession> rematch(String sessionId, String userId) {
        return sessionFor(sessionId, userId).flatMap(session -> {
            if (session.getStatus() != GameStatus.FINISHED) {
                return Mono.error(new BadRequestException("This game is still in progress."));
            }

            return sessions.findByRematchOfSessionId(session.getId())
                    .flatMap(existing -> Mono.<GameSession>error(new BadRequestException(
                            "A rematch of this game already exists: " + existing.getId())))
                    .switchIfEmpty(Mono.defer(() -> {
                        String loser = session.getParticipants().stream()
                                .map(GameSession.Participant::getUserId)
                                .filter(player -> !player.equals(session.getWinnerUserId()))
                                .findFirst()
                                .orElse(session.getParticipants().get(0).getUserId());

                        return startSession(session.getParticipants(), loser, session.getId());
                    }));
        });
    }

    private Mono<GameSession> activeSessionFor(String sessionId, String userId) {
        return sessionFor(sessionId, userId).flatMap(session -> session.getStatus() != GameStatus.IN_PROGRESS
                ? Mono.error(new BadRequestException("This game is already finished."))
                : Mono.just(session));
    }

    private boolean turnHasExpired(GameSession session) {
        return session.getTurnDeadline() != null && Instant.now(clock).isAfter(session.getTurnDeadline());
    }

    private void finish(GameSession session, String winnerUserId, EndReason reason) {
        session.setStatus(GameStatus.FINISHED);
        session.setWinnerUserId(winnerUserId);
        session.setEndReason(reason);
        session.setFinishedAt(Instant.now(clock));
        session.setCurrentTurnUserId(null);
        session.setTurnDeadline(null);
    }

    private GameSession applyResult(GameSession session, String userId, GameSession.Participant opponent,
                                    CombatResultResponse result) {
        CombatResultResponse.TargetOutcomeResponse outcome = result.outcomes() == null || result.outcomes().isEmpty()
                ? null
                : result.outcomes().get(0);

        session.getTurns().add(new GameSession.TurnRecord(
                session.getTurnNumber(),
                userId,
                result.abilityId(),
                result.abilityName(),
                outcome == null ? 0 : outcome.amount(),
                outcome == null ? null : outcome.targetId(),
                outcome == null ? 0 : outcome.resultingHealth(),
                outcome != null && outcome.defeated(),
                Instant.now(clock)));

        boolean opponentDefeated = outcome != null
                && outcome.defeated()
                && opponent.getCharacterId().equals(outcome.targetId());

        if (opponentDefeated) {
            finish(session, userId, EndReason.DEFEAT);
            log.info("Session {} won by {}", session.getId(), userId);
        } else {
            opponent.setMovementRemaining(movementPerTurn);
            session.setCurrentTurnUserId(opponent.getUserId());
            session.setTurnNumber(session.getTurnNumber() + 1);
            session.setTurnDeadline(Instant.now(clock).plus(turnTimeout));
        }

        return session;
    }
}
