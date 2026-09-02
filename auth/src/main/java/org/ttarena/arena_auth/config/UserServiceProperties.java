package org.ttarena.arena_auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Where the user service lives and how patient we are with it.
 */
@Validated
@ConfigurationProperties(prefix = "user-service")
public record UserServiceProperties(

        @NotBlank(message = "user-service.base-url is required")
        String baseUrl,

        Duration responseTimeout,

        int maxRetries,

        Duration retryBackoff) {

    public UserServiceProperties {
        responseTimeout = responseTimeout == null ? Duration.ofSeconds(5) : responseTimeout;
        retryBackoff = retryBackoff == null ? Duration.ofMillis(200) : retryBackoff;
        maxRetries = maxRetries <= 0 ? 1 : maxRetries;
    }
}
