package org.ttarena.arena_user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_user.dto.ArenaUserResponse;
import org.ttarena.arena_user.dto.RegisterUserRequest;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;

@Tag(name = "User", description = "the User API")
@RestController
@RequestMapping("/user")
public class UserController {

	private final ArenaUserService arenaUserService;

	public UserController(ArenaUserService arenaUserService) {
		this.arenaUserService = arenaUserService;
	}

	@Operation(summary = "Register", description = "Creates a new account.")
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<ArenaUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		return arenaUserService.register(request).map(ArenaUserResponse::from);
	}

	@Operation(summary = "User home page", description = "Home page for logged in users.")
	@GetMapping("/home")
	public Mono<ResponseEntity<String>> handleUserWelcome() {
		return Mono.just(ResponseEntity.ok("You are now logged in, welcome!"));
	}
}
