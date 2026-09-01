package org.ttarena.arena_game.dto;

import org.ttarena.arena_game.document.GameSession;
import org.ttarena.arena_game.document.GameStatus;

import java.time.Instant;
import java.util.List;

public record GameSessionResponse(
        String id,
        GameStatus status,
        int turnNumber,
        String currentTurnUserId,
        boolean yourTurn,
        String yourCharacterId,
        String opponentUserId,
        String opponentCharacterId,
        String winnerUserId,
        org.ttarena.arena_game.document.EndReason endReason,
        Instant turnDeadline,
        Instant createdAt,
        Instant finishedAt,
        List<GameSession.TurnRecord> turns) {

    public static GameSessionResponse of(GameSession session, String userId) {
        GameSession.Participant you = session.participantOf(userId);
        GameSession.Participant opponent = session.opponentOf(userId);

        return new GameSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getTurnNumber(),
                session.getCurrentTurnUserId(),
                userId.equals(session.getCurrentTurnUserId()),
                you == null ? null : you.getCharacterId(),
                opponent == null ? null : opponent.getUserId(),
                opponent == null ? null : opponent.getCharacterId(),
                session.getWinnerUserId(),
                session.getEndReason(),
                session.getTurnDeadline(),
                session.getCreatedAt(),
                session.getFinishedAt(),
                session.getTurns());
    }
}
