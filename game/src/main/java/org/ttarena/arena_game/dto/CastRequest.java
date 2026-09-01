package org.ttarena.arena_game.dto;

import jakarta.validation.constraints.NotBlank;

public record CastRequest(

        @NotBlank(message = "abilityId is required")
        String abilityId) {
}
