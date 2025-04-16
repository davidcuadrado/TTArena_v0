package org.ttarena.arena_auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.helper.AuthResponse;
import org.ttarena.arena_auth.service.JwtService;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private ReactiveAuthenticationManager authenticationManager;
    private JwtService jwtService;
    private WebClient.Builder webClientBuilder;

    public AuthController(ReactiveAuthenticationManager authenticationManager, JwtService jwtService, WebClient.Builder webClientBuilder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.webClientBuilder = webClientBuilder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return webClientBuilder.build()
                .post()
                .uri("http://user-service/users/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserDetails.class)
                .flatMap(userDetails -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails.getUsername(), request.getPassword());

                    return authenticationManager.authenticate(auth)
                            .flatMap(authResult -> jwtService.generateToken(Mono.just(userDetails)))
                            .map(jwt -> ResponseEntity.ok(new AuthResponse(jwt)));
                });
    }
}
