package org.ttarena.arena_auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.ttarena.arena_auth.client.UserServiceClient;
import org.ttarena.arena_auth.dto.AuthenticatedUser;
import org.ttarena.arena_auth.exception.AuthenticationFailedException;
import org.ttarena.arena_auth.exception.UserServiceFailedException;
import org.ttarena.arena_auth.exception.UserServiceUnavailableException;
import org.ttarena.arena_auth.helper.AuthRequest;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;
import org.ttarena.arena_auth.service.JwtService;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Guards the wiring rather than the logic: that login is reachable without a
 * token, that everything else is not, that CORS answers a browser preflight,
 * and that failures come back as problem details.
 *
 * <p>Login was once unreachable from two directions at once - a default security
 * chain and a filter whose public paths disagreed with it. These are the tests
 * that would have caught it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthSecurityIntegrationTest {

    private static final AuthenticatedUser ALICE =
            new AuthenticatedUser("user-1", "alice", "alice@ttarena.org", List.of("USER"));

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private WebTestClient webTestClient;

    /**
     * Bound to a real server rather than to the application context: CORS is
     * partly handled by the HTTP layer, and a context-bound client does not
     * reproduce it faithfully.
     */
    @BeforeEach
    void bindToServer() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private WebTestClient.ResponseSpec postLogin(String username, String password) {
        return webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequest(username, password))
                .exchange();
    }

    @Test
    void loginIsReachableWithoutAToken() {
        when(userServiceClient.authenticate(any())).thenReturn(Mono.just(ALICE));

        postLogin("alice", "password123")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    void everythingElseNeedsAToken() {
        webTestClient.get().uri("/auth/whoami").exchange().expectStatus().isUnauthorized();
    }

    /**
     * A valid token gets past security, so the 404 here is the router saying
     * "no such endpoint" - which means the JWT filter authenticated the request.
     */
    @Test
    void aValidTokenGetsPastSecurity() {
        String token = jwtService.generateToken(AuthenticatedUserPrincipal.of(ALICE)).block();

        webTestClient.get()
                .uri("/auth/whoami")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void aBrowserPreflightIsAnswered() {
        webTestClient.options()
                .uri("/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
    }

    /**
     * The login POST itself is a simple cross-origin request: without this header
     * the browser discards the response even though the server processed it.
     */
    @Test
    void aSimpleCrossOriginRequestGetsTheAllowOriginHeader() {
        when(userServiceClient.authenticate(any())).thenReturn(Mono.just(ALICE));

        webTestClient.post()
                .uri("/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequest("alice", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
    }

    @Test
    void anUnknownOriginIsNotAllowed() {
        webTestClient.options()
                .uri("/auth/login")
                .header(HttpHeaders.ORIGIN, "http://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void blankCredentialsAreRejectedAsAProblemDetail() {
        postLogin("", "")
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Invalid request");
    }

    /**
     * Deliberately bare: a 401 that explains itself tells an attacker which half
     * of the credentials was wrong.
     */
    @Test
    void rejectedCredentialsSayNothingBeyondUnauthorized() {
        when(userServiceClient.authenticate(any()))
                .thenReturn(Mono.error(new AuthenticationFailedException("rejected")));

        postLogin("alice", "wrong")
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.detail").doesNotExist();
    }

    @Test
    void anUnreachableUserServiceIsA503ProblemDetail() {
        when(userServiceClient.authenticate(any())).thenReturn(Mono.error(
                new UserServiceUnavailableException("down", new RuntimeException())));

        postLogin("alice", "password123")
                .expectStatus().isEqualTo(503)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Sign-in is temporarily unavailable");
    }

    @Test
    void aFailingUserServiceIsA502ProblemDetail() {
        when(userServiceClient.authenticate(any()))
                .thenReturn(Mono.error(new UserServiceFailedException("boom")));

        postLogin("alice", "password123")
                .expectStatus().isEqualTo(502)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Sign-in failed upstream");
    }
}
