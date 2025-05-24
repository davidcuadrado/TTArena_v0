package org.ttarena.arena_user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;
import org.ttarena.arena_user.service.JwtService;

@Tag(name = "User", description = "the User API")
@RestController
@RequestMapping("/user")
public class UserController {

	private final ArenaUserService arenaUserService;
	private final JwtService jwtService;

	public UserController(ArenaUserService arenaUserService, JwtService jwtService) {
		this.arenaUserService = arenaUserService;
		this.jwtService = jwtService;
	}

	@Operation(summary = "User home page", description = "Home page for logged in users.")
	@GetMapping("/home")
	public Mono<ResponseEntity<String>> handleUserWelcome() {
		return Mono.just(ResponseEntity.ok("You are now logged in, welcome!"));
	}


}
