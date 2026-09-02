package org.ttarena.arena_auth.dto;

import java.util.List;

public record AuthenticatedUser(String userId, String username, String email, List<String> roles) {
}
