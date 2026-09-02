package org.ttarena.arena_user.util;

import java.time.Instant;

public class RedisEvent {
    private String type;
    private String userId;
    private String characterId;
    private Instant timestamp;

    public RedisEvent() {
    }

    public RedisEvent(String type, String userId, String characterId, Instant timestamp) {
        this.type = type;
        this.userId = userId;
        this.characterId = characterId;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
