package org.ttarena.arena_game.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "game_sessions")
public class GameSession {

    @Id
    private String id;

    @Version
    private Long version;

    private List<Participant> participants;

    @Indexed
    private String currentTurnUserId;

    private GameStatus status;
    private int turnNumber;
    private String winnerUserId;
    private EndReason endReason;
    private Instant turnDeadline;
    private Instant createdAt;
    private Instant finishedAt;
    private String rematchOfSessionId;

    private String arenaMapId;

    @Builder.Default
    private List<TurnRecord> turns = new ArrayList<>();

    public Participant participantOf(String userId) {
        return participants.stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public Participant opponentOf(String userId) {
        return participants.stream()
                .filter(participant -> !participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        private String userId;
        private String characterId;
        private HexCoordinate position;
        private int movementRemaining;

        public Participant(String userId, String characterId) {
            this(userId, characterId, null, 0);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnRecord {
        private int turnNumber;
        private String userId;
        private String abilityId;
        private String abilityName;
        private int amount;
        private String targetCharacterId;
        private int targetRemainingHealth;
        private boolean targetDefeated;
        private Instant playedAt;
    }
}
