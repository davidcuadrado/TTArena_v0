package org.ttarena.arena_auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ttarena.arena_auth.dto.AuthenticatedUser;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.helper.AuthResponse;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;
import org.ttarena.arena_auth.service.JwtService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final WebClient userServiceClient;

    public AuthController(JwtService jwtService,
                          WebClient.Builder webClientBuilder,
                          @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.jwtService = jwtService;
        this.userServiceClient = webClientBuilder.baseUrl(userServiceBaseUrl).build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return userServiceClient.post()
                .uri("/users/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticatedUser.class)
                .map(AuthenticatedUserPrincipal::of)
                .flatMap(userDetails -> jwtService.generateToken(Mono.just(userDetails)))
                .map(token -> ResponseEntity.ok(new AuthResponse(token)))
                .onErrorResume(WebClientResponseException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

}
