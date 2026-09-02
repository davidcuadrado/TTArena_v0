package org.ttarena.arena_auth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ttarena.arena_auth.config.UserServiceProperties;
import org.ttarena.arena_auth.dto.AuthenticatedUser;
import org.ttarena.arena_auth.exception.AuthenticationFailedException;
import org.ttarena.arena_auth.exception.UserServiceFailedException;
import org.ttarena.arena_auth.exception.UserServiceUnavailableException;
import org.ttarena.arena_auth.helper.AuthRequest;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.concurrent.TimeoutException;

/**
 * Verifies credentials against the user service.
 *
 * <p>Only a 4xx from that service means "bad credentials". A 5xx, a timeout or
 * a refused connection are its failures, not the caller's, and are reported as
 * such rather than as a rejected login.
 */
@Slf4j
@Component
public class UserServiceClient {

    private final WebClient webClient;
    private final UserServiceProperties properties;

    public UserServiceClient(WebClient.Builder builder, UserServiceProperties properties) {
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    public Mono<AuthenticatedUser> authenticate(AuthRequest request) {
        return webClient.post()
                .uri("/users/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticatedUser.class)
                .timeout(properties.responseTimeout())
                // Only connection-level failures are retried: a 4xx is a decision,
                // and replaying it would just be a second wrong password.
                .retryWhen(Retry.backoff(properties.maxRetries(), properties.retryBackoff())
                        .filter(UserServiceClient::isTransient)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .onErrorMap(WebClientResponseException.class, UserServiceClient::translate)
                .onErrorMap(UserServiceClient::isTransient,
                        e -> new UserServiceUnavailableException("user service did not answer", e));
    }

    private static boolean isTransient(Throwable e) {
        return e instanceof WebClientRequestException || e instanceof TimeoutException;
    }

    private static Throwable translate(WebClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            return new AuthenticationFailedException("credentials rejected by the user service");
        }
        return new UserServiceFailedException("user service returned " + e.getStatusCode());
    }
}
