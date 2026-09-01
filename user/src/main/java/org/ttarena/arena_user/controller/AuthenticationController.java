package org.ttarena.arena_user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_user.dto.ArenaUserResponse;
import org.ttarena.arena_user.dto.AuthenticationRequest;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;

@Tag(name = "Authentication", description = "credential verification for the auth service")
@RestController
@RequestMapping("/users")
public class AuthenticationController {

	private final ArenaUserService arenaUserService;

	public AuthenticationController(ArenaUserService arenaUserService) {
		this.arenaUserService = arenaUserService;
	}

	@Operation(summary = "Verify credentials",
			description = "Called service-to-service by the auth module. Returns the account on success, 400 otherwise.")
	@PostMapping("/authenticate")
	public Mono<ArenaUserResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
		return arenaUserService.authenticate(request).map(ArenaUserResponse::from);
	}
}
