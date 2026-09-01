package org.ttarena.arena_game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ttarena.arena_game.client.CharacterServiceClient;
import org.ttarena.arena_game.client.CombatResultResponse;
import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.repository.GameSessionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GameSessionService {

    private final GameSessionRepository sessions;
    private final CharacterServiceClient characterService;

    public GameSessionService(GameSessionRepository sessions, CharacterServiceClient characterService) {
        this.sessions = sessions;
        this.characterService = characterService;
    }

    public Mono<GameSession> startSession(List<GameSession.Participant> participants) {
        if (participants == null || participants.size() != 2) {
            return Mono.error(new BadRequestException("A session needs exactly two participants."));
        }

        GameSession session = GameSession.builder()
                .participants(participants)
                // The player who queued first moves first.
                .currentTurnUserId(participants.get(0).getUserId())
                .status(GameStatus.IN_PROGRESS)
                .turnNumber(1)
                .createdAt(Instant.now())
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

    /**
     * Plays one turn. The session decides whether it is your turn and which
     * character is yours; the character service decides whether the cast itself
     * is legal and how much it does.
     */
    public Mono<GameSession> cast(String sessionId, String userId, String bearerToken, String abilityId) {
        return sessionFor(sessionId, userId).flatMap(session -> {
            if (session.getStatus() != GameStatus.IN_PROGRESS) {
                return Mono.error(new BadRequestException("This game is already finished."));
            }
            if (!userId.equals(session.getCurrentTurnUserId())) {
                return Mono.error(new ForbiddenException("It is not your turn."));
            }

            GameSession.Participant you = session.participantOf(userId);
            GameSession.Participant opponent = session.opponentOf(userId);
            if (opponent == null) {
                return Mono.error(new BadRequestException("This game has no opponent."));
            }

            return characterService
                    .cast(bearerToken, you.getCharacterId(), abilityId, List.of(opponent.getCharacterId()))
                    .map(result -> applyResult(session, userId, opponent, result))
                    .flatMap(sessions::save);
        });
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
                Instant.now()));

        boolean opponentDefeated = outcome != null
                && outcome.defeated()
                && opponent.getCharacterId().equals(outcome.targetId());

        if (opponentDefeated) {
            session.setStatus(GameStatus.FINISHED);
            session.setWinnerUserId(userId);
            session.setFinishedAt(Instant.now());
            session.setCurrentTurnUserId(null);
            log.info("Session {} won by {}", session.getId(), userId);
        } else {
            session.setCurrentTurnUserId(opponent.getUserId());
            session.setTurnNumber(session.getTurnNumber() + 1);
        }

        return session;
    }
}
