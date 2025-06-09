package org.ttarena.matchmaking.document;

import java.time.Instant;
import java.util.List;

public class MatchFoundEvent {
    private String type;
    private List<String> players;
    private Instant timestamp;

    // Constructors
    public MatchFoundEvent() {}

    public MatchFoundEvent(String type, List<String> players, Instant timestamp) {
        this.type = type;
        this.players = players;
        this.timestamp = timestamp;
    }

    // Getters & setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
