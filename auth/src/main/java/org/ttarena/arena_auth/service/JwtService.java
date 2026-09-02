package org.ttarena.arena_auth.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.security.converter.RsaKeyConverters;
import org.ttarena.arena_auth.config.JwtProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

/**
 * Mints and verifies RS256 tokens. This is the only service in the project that
 * holds the private key; everyone else verifies with the public one.
 *
 * <p>The claims are a contract: {@code sub} is the username (the user service
 * looks accounts up by it) and {@code userId} is the account UUID (character,
 * matchmaking and game key ownership on it).
 */
@Service
public class JwtService {

    public static final String ISSUER = "TTArena_v0";
    public static final String USER_ID_CLAIM = "userId";
    public static final String ROLES_CLAIM = "roles";

    private static final String BEARER_PREFIX = "Bearer ";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final Duration validity;

    public JwtService(JwtProperties properties) throws IOException {
        try (InputStream in = properties.privateKey().getInputStream()) {
            this.privateKey = RsaKeyConverters.pkcs8().convert(in);
        }
        try (InputStream in = properties.publicKey().getInputStream()) {
            this.publicKey = RsaKeyConverters.x509().convert(in);
        }
        this.validity = Duration.ofMinutes(properties.ttlMinutes());
    }

    public Mono<String> generateToken(UserDetails userDetails) {
        return Mono.fromCallable(() -> {
            Instant issuedAt = Instant.now();

            return Jwts.builder()
                    .issuer(ISSUER)
                    .subject(userDetails.getUsername())
                    .claim(USER_ID_CLAIM, resolveUserId(userDetails))
                    .claim(ROLES_CLAIM, userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(issuedAt.plus(validity)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        });
    }

    private String resolveUserId(UserDetails userDetails) {
        return userDetails instanceof AuthenticatedUserPrincipal principal
                ? principal.getUserId()
                : userDetails.getUsername();
    }

    /**
     * @return the username, or an error if the token is unreadable, unsigned by
     *         us, expired, or issued by someone else.
     */
    public Mono<String> validateAndExtractUsername(String token) {
        return claims(token)
                .map(Claims::getSubject)
                .onErrorMap(JwtException.class, e -> new IllegalArgumentException("Invalid JWT token", e));
    }

    public Mono<String> extractUsername(String token) {
        return claims(token).map(Claims::getSubject);
    }

    public Mono<String> extractUserId(String token) {
        return claims(token).flatMap(payload -> Mono.justOrEmpty(payload.get(USER_ID_CLAIM, String.class)));
    }

    public Mono<List<String>> extractUserRoles(String token) {
        return claims(token).map(payload -> {
            Object roles = payload.get(ROLES_CLAIM);
            if (roles instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            throw new JwtException("Roles claim is not a list");
        });
    }

    public Mono<Boolean> isTokenValid(String token) {
        return claims(token).map(payload -> true).onErrorReturn(false);
    }

    /**
     * Parses once and lets jjwt do the work: signature, expiry and issuer are all
     * checked here, so nothing downstream has to re-check them by hand.
     */
    private Mono<Claims> claims(String token) {
        return Mono.fromCallable(() -> Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(stripBearer(token))
                .getPayload());
    }

    private static String stripBearer(String token) {
        if (token == null) {
            throw new JwtException("No token supplied");
        }
        return token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
    }
}
