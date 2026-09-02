package org.ttarena.arena_auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.helper.AuthResponse;
import org.ttarena.arena_auth.service.AuthenticationService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return authenticationService.login(request).map(AuthResponse::new);
    }
}
