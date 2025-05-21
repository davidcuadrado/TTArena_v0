package org.ttarena.arena_user.util;

import java.time.Instant;

public class RedisEvent {
    private String type;
    private String userId;
    private Instant timestamp;

    public RedisEvent() {
    }

    public RedisEvent(String type, String userId, Instant timestamp) {
        this.type = type;
        this.userId = userId;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
