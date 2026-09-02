package org.ttarena.arena_auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.ttarena.arena_auth.client.UserServiceClient;
import org.ttarena.arena_auth.config.JwtProperties;
import org.ttarena.arena_auth.config.UserServiceProperties;
import org.ttarena.arena_auth.exception.AuthenticationFailedException;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.helper.AuthResponse;
import org.ttarena.arena_auth.service.AuthenticationService;
import org.ttarena.arena_auth.service.JwtService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    private static final AuthRequest LOGIN = new AuthRequest("alice", "password123");

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws IOException {
        jwtService = new JwtService(new JwtProperties(
                new ClassPathResource("keys/dev-private.pem"),
                new ClassPathResource("keys/dev-public.pem"),
                30));
    }

    private AuthController controllerAnswering(ExchangeFunction exchange) {
        UserServiceClient client = new UserServiceClient(
                WebClient.builder().exchangeFunction(exchange),
                new UserServiceProperties("http://user-service", Duration.ofSeconds(5), 1, Duration.ofMillis(1)));

        return new AuthController(new AuthenticationService(client, jwtService));
    }

    @Test
    void aVerifiedAccountComesBackAsASignedToken() {
        AuthController controller = controllerAnswering(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {"userId":"user-1","username":"alice","email":"alice@ttarena.org","roles":["USER"]}
                        """)
                .build()));

        AuthResponse response = controller.login(LOGIN).block();

        assertThat(response).isNotNull();
        String token = response.token();

        StepVerifier.create(jwtService.extractUsername(token)).expectNext("alice").verifyComplete();
        StepVerifier.create(jwtService.extractUserId(token)).expectNext("user-1").verifyComplete();
        StepVerifier.create(jwtService.extractUserRoles(token))
                .expectNext(List.of("ROLE_USER"))
                .verifyComplete();
    }

    @Test
    void badCredentialsSurfaceAsAFailedAuthentication() {
        AuthController controller = controllerAnswering(request -> Mono.just(
                ClientResponse.create(HttpStatus.BAD_REQUEST)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("nope")
                        .build()));

        StepVerifier.create(controller.login(LOGIN))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }
}
