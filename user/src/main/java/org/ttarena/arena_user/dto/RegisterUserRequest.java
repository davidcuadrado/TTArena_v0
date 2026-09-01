package org.ttarena.arena_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

		@NotBlank(message = "username is required")
		@Size(min = 3, max = 32, message = "username must be between 3 and 32 characters")
		String username,

		@NotBlank(message = "email is required")
		@Email(message = "email must be a valid address")
		String email,

		@NotBlank(message = "password is required")
		@Size(min = 8, message = "password must be at least 8 characters")
		String password) {
}
