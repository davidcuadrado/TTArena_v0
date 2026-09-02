package org.ttarena.arena_auth.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * Signing material and token lifetime. Validated so a missing key or a zero TTL
 * stops the application at startup rather than at the first login.
 */
@Validated
@ConfigurationProperties(prefix = "ttarena.jwt")
public record JwtProperties(

        @NotNull(message = "ttarena.jwt.private-key is required")
        Resource privateKey,

        @NotNull(message = "ttarena.jwt.public-key is required")
        Resource publicKey,

        @Positive(message = "ttarena.jwt.ttl-minutes must be greater than 0")
        long ttlMinutes) {
}
