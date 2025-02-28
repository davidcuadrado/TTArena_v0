package org.ttarena.arena_user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;
import services.JwtService;

@Tag(name = "User", description = "the User API")
@RestController
@RequestMapping("/user")
public class UserController {

	private ArenaUserService arenaUserService;

	private JwtService jwtService;

	public UserController(ArenaUserService arenaUserService, JwtService jwtService) {
		this.arenaUserService = arenaUserService;
		this.jwtService = jwtService;
	}

	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "User home page", description = "Home page for logged in users.")
	@GetMapping("/home")
	public Mono<ResponseEntity<String>> handleUserWelcome() {
		return Mono.just(ResponseEntity.ok("You are now logged in, welcome!"));
	}


}
