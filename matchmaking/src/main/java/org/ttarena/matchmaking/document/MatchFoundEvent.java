package org.ttarena.matchmaking.document;

import java.time.Instant;
import java.util.List;

public class MatchFoundEvent {
    private String type;
    private List<Participant> participants;
    private Instant timestamp;

    public MatchFoundEvent() {}

    public MatchFoundEvent(String type, List<Participant> participants, Instant timestamp) {
        this.type = type;
        this.participants = participants;
        this.timestamp = timestamp;
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

        public Participant() {}

        public Participant(String userId, String characterId) {
            this.userId = userId;
            this.characterId = characterId;
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
