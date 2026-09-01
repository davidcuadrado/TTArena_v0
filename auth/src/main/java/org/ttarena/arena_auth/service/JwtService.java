package org.ttarena.arena_auth.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import reactor.core.publisher.Mono;

/**
 * Mints and verifies RS256 tokens. This is the only service in the project that
 * holds the private key; everyone else verifies with the public one.
 */
@Service
public class JwtService {

    private static final long VALIDITY = TimeUnit.MINUTES.toMillis(30);

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtService(@Value("${ttarena.jwt.private-key}") Resource privateKeyPem,
                      @Value("${ttarena.jwt.public-key}") Resource publicKeyPem) throws IOException {
        try (InputStream in = privateKeyPem.getInputStream()) {
            this.privateKey = RsaKeyConverters.pkcs8().convert(in);
        }
        try (InputStream in = publicKeyPem.getInputStream()) {
            this.publicKey = RsaKeyConverters.x509().convert(in);
        }
    }

    public Mono<String> generateToken(Mono<UserDetails> userDetailsMono) {
        return userDetailsMono.flatMap(userDetails -> Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", "TTArena_v0");
            claims.put("userId", resolveUserId(userDetails));
            claims.put("roles",
                    userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

            return Jwts.builder()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(VALIDITY)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        }));
    }

    private String resolveUserId(UserDetails userDetails) {
        return userDetails instanceof AuthenticatedUserPrincipal principal
                ? principal.getUserId()
                : userDetails.getUsername();
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
            try {
                Object rolesObject = getClaims(token).get("roles");

                if (rolesObject instanceof List<?> roles) {
                    return roles.stream().map(Object::toString).toList();
                }
                throw new IllegalArgumentException("Roles claim is not a valid list");
            } catch (JwtException | IllegalArgumentException e) {
                throw new JwtException("Error extracting roles from token", e);
            }
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
