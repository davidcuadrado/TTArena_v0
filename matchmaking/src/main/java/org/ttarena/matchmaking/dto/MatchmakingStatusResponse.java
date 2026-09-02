package org.ttarena.matchmaking.dto;

import org.ttarena.matchmaking.document.MatchFoundEvent;

import java.time.Instant;

public record MatchmakingStatusResponse(String userId, boolean queued, int queueSize, MatchSummary match) {

    public record MatchSummary(String opponentId, String opponentCharacterId, String yourCharacterId,
                               Instant foundAt) {
    }

    public static MatchmakingStatusResponse of(String userId, boolean queued, int queueSize, MatchFoundEvent match) {
        if (match == null) {
            return new MatchmakingStatusResponse(userId, queued, queueSize, null);
        }

        MatchFoundEvent.Participant you = match.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        MatchFoundEvent.Participant opponent = match.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        return new MatchmakingStatusResponse(userId, queued, queueSize, new MatchSummary(
                opponent == null ? null : opponent.getUserId(),
                opponent == null ? null : opponent.getCharacterId(),
                you == null ? null : you.getCharacterId(),
                match.getTimestamp()));
    }
}
