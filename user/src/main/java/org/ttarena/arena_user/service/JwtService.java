package org.ttarena.arena_user.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import reactor.core.publisher.Mono;

/**
 * Verifies tokens minted by the auth service. This module holds the public key
 * only: it can check a token, never issue one.
 */
@Service
public class JwtService {

    private final RSAPublicKey publicKey;

    public JwtService(@Value("${ttarena.jwt.public-key}") Resource publicKeyPem) throws IOException {
        try (InputStream in = publicKeyPem.getInputStream()) {
            this.publicKey = RsaKeyConverters.x509().convert(in);
        }
    }

    public Mono<String> validateAndExtractUsername(String token) {
        return isTokenValid(token).flatMap(valid -> {
            if (Boolean.TRUE.equals(valid)) {
                return extractUsername(token);
            } else {
                return Mono.error(new IllegalArgumentException("Invalid JWT token"));
            }
        });
    }

    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() -> {
            try {
                return getClaims(token).getSubject();
            } catch (SignatureException e) {
                throw new IllegalArgumentException("Invalid JWT token signature", e);
            }
        });
    }

    public Mono<String> extractUserId(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        return Mono.fromCallable(() -> getClaims(cleanToken).get("userId", String.class));
    }

    public Mono<List<String>> extractUserRoles(String token) {
        return Mono.fromCallable(() -> {
            Object rolesObject = getClaims(token).get("roles");

            if (rolesObject instanceof List<?> roles) {
                return roles.stream().map(Object::toString).toList();
            }
            throw new JwtException("Roles claim is not a valid list");
        });
    }

    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public Mono<Boolean> isTokenValid(String jwt) {
        return Mono.fromCallable(() -> {
            try {
                Date expiration = getClaims(jwt).getExpiration();
                return expiration != null && expiration.after(Date.from(Instant.now()));
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        });
    }
}
