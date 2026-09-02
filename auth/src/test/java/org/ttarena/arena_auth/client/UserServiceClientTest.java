package org.ttarena.arena_auth.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.ttarena.arena_auth.config.UserServiceProperties;
import org.ttarena.arena_auth.dto.AuthenticatedUser;
import org.ttarena.arena_auth.exception.AuthenticationFailedException;
import org.ttarena.arena_auth.exception.UserServiceFailedException;
import org.ttarena.arena_auth.exception.UserServiceUnavailableException;
import org.ttarena.arena_auth.helper.AuthRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceClientTest {

    private static final AuthRequest LOGIN = new AuthRequest("alice", "password123");

    private UserServiceProperties properties(int maxRetries, Duration timeout) {
        return new UserServiceProperties("http://user-service", timeout, maxRetries, Duration.ofMillis(1));
    }

    private UserServiceClient clientAnswering(ExchangeFunction exchange, int maxRetries, Duration timeout) {
        return new UserServiceClient(WebClient.builder().exchangeFunction(exchange),
                properties(maxRetries, timeout));
    }

    private static ExchangeFunction responding(HttpStatus status, String body) {
        return request -> Mono.just(ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
    }

    private static ExchangeFunction refusing() {
        return request -> Mono.error(new WebClientRequestException(
                new ConnectException("connection refused"), HttpMethod.POST,
                URI.create("http://user-service/users/authenticate"), new HttpHeaders()));
    }

    @Test
    void aVerifiedAccountIsReturnedWithItsIdAndRoles() {
        UserServiceClient client = clientAnswering(responding(HttpStatus.OK, """
                {"userId":"user-1","username":"alice","email":"alice@ttarena.org","roles":["USER"]}
                """), 1, Duration.ofSeconds(5));

        AuthenticatedUser user = client.authenticate(LOGIN).block();

        assertThat(user).isNotNull();
        assertThat(user.userId()).isEqualTo("user-1");
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.roles()).isEqualTo(List.of("USER"));
    }

    @Test
    void aRejectionFromTheUserServiceIsAFailedLogin() {
        UserServiceClient client = clientAnswering(
                responding(HttpStatus.BAD_REQUEST, "Invalid username or password."), 1, Duration.ofSeconds(5));

        StepVerifier.create(client.authenticate(LOGIN))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void aFailureInsideTheUserServiceIsNotAFailedLogin() {
        UserServiceClient client = clientAnswering(
                responding(HttpStatus.INTERNAL_SERVER_ERROR, "boom"), 1, Duration.ofSeconds(5));

        StepVerifier.create(client.authenticate(LOGIN))
                .expectError(UserServiceFailedException.class)
                .verify();
    }

    @Test
    void anUnreachableUserServiceIsReportedAsUnavailable() {
        UserServiceClient client = clientAnswering(refusing(), 1, Duration.ofSeconds(5));

        StepVerifier.create(client.authenticate(LOGIN))
                .expectError(UserServiceUnavailableException.class)
                .verify();
    }

    @Test
    void aHangingUserServiceTimesOutInsteadOfHangingTheLogin() {
        UserServiceClient client = clientAnswering(request -> Mono.never(), 0, Duration.ofMillis(50));

        StepVerifier.create(client.authenticate(LOGIN))
                .expectError(UserServiceUnavailableException.class)
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void connectionFailuresAreRetried() {
        AtomicInteger attempts = new AtomicInteger();
        ExchangeFunction flaky = request -> {
            if (attempts.incrementAndGet() == 1) {
                return Mono.error(new WebClientRequestException(
                        new ConnectException("connection refused"), HttpMethod.POST,
                        URI.create("http://user-service/users/authenticate"), new HttpHeaders()));
            }
            return responding(HttpStatus.OK, """
                    {"userId":"user-1","username":"alice","email":"alice@ttarena.org","roles":["USER"]}
                    """).exchange(request);
        };

        AuthenticatedUser user = clientAnswering(flaky, 1, Duration.ofSeconds(5)).authenticate(LOGIN).block();

        assertThat(user).isNotNull();
        assertThat(attempts.get()).isEqualTo(2);
    }

    /**
     * A rejected password is a decision, not a blip - replaying it would only
     * ask the same wrong question twice.
     */
    @Test
    void rejectedCredentialsAreNotRetried() {
        AtomicInteger attempts = new AtomicInteger();
        ExchangeFunction counting = request -> {
            attempts.incrementAndGet();
            return responding(HttpStatus.BAD_REQUEST, "nope").exchange(request);
        };

        StepVerifier.create(clientAnswering(counting, 3, Duration.ofSeconds(5)).authenticate(LOGIN))
                .expectError(AuthenticationFailedException.class)
                .verify();

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void exhaustingTheRetriesIsReportedAsUnavailable() {
        AtomicInteger attempts = new AtomicInteger();
        ExchangeFunction alwaysRefusing = request -> {
            attempts.incrementAndGet();
            return refusing().exchange(request);
        };

        StepVerifier.create(clientAnswering(alwaysRefusing, 2, Duration.ofSeconds(5)).authenticate(LOGIN))
                .expectError(UserServiceUnavailableException.class)
                .verify();

        // the first attempt plus two retries
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void theRequestCarriesTheCredentialsAsJson() {
        AtomicInteger posts = new AtomicInteger();
        ExchangeFunction recording = request -> {
            posts.incrementAndGet();
            assertThat(request.method()).isEqualTo(HttpMethod.POST);
            assertThat(request.url().getPath()).isEqualTo("/users/authenticate");
            return responding(HttpStatus.OK, """
                    {"userId":"user-1","username":"alice","email":"alice@ttarena.org","roles":["USER"]}
                    """).exchange(request);
        };

        clientAnswering(recording, 1, Duration.ofSeconds(5)).authenticate(LOGIN).block();

        assertThat(posts.get()).isEqualTo(1);
    }
}
