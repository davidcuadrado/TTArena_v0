package org.ttarena.arena_auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ttarena.arena_auth.client.UserServiceClient;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;
import reactor.core.publisher.Mono;

/**
 * Verify credentials, then mint a token for the account they belong to.
 */
@Slf4j
@Service
public class AuthenticationService {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;

    public AuthenticationService(UserServiceClient userServiceClient, JwtService jwtService) {
        this.userServiceClient = userServiceClient;
        this.jwtService = jwtService;
    }

    public Mono<String> login(AuthRequest request) {
        return userServiceClient.authenticate(request)
                .map(AuthenticatedUserPrincipal::of)
                .flatMap(jwtService::generateToken)
                // Usernames only: never the password, never the token itself.
                .doOnSuccess(token -> log.info("Issued a token for '{}'", request.username()))
                .doOnError(e -> log.info("Login refused for '{}': {}",
                        request.username(), e.getClass().getSimpleName()));
    }
}
