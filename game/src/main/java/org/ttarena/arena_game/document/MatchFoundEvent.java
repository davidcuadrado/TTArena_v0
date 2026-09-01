package org.ttarena.arena_game.document;

import java.time.Instant;
import java.util.List;

/**
 * The matchmaking service's {@code match.found} payload. Kept in step with
 * the publisher's shape by hand - there is no shared contract module yet.
 */
public class MatchFoundEvent {

    private String type;
    private List<Participant> participants;
    private Instant timestamp;

    public MatchFoundEvent() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static class Participant {
        private String userId;
        private String characterId;

        public Participant() {
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCharacterId() {
            return characterId;
        }

        public void setCharacterId(String characterId) {
            this.characterId = characterId;
        }
    }
}
