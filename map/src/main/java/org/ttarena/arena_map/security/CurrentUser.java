package org.ttarena.arena_map.security;

import java.util.List;

public record CurrentUser(String userId, String username, List<String> roles) {

    public boolean owns(String ownerId) {
        return userId != null && userId.equals(ownerId);
    }
}
