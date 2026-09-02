package org.ttarena.arena_user.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(

		@NotBlank(message = "username is required")
		String username,

		@NotBlank(message = "password is required")
		String password) {
}
