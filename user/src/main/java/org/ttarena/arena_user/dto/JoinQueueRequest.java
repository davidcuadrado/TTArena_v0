package org.ttarena.arena_user.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinQueueRequest(

		@NotBlank(message = "characterId is required")
		String characterId) {
}
