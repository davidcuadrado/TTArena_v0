package org.ttarena.arena_auth.helper;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password) {
}
