package org.ttarena.arena_auth.service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

    private static final String SECRET = "E0D1A0FDE7DECE0CF1FB3212E468DCCAA9B8707334E6CD50F0DEB47FE679FFF3";
    private static final long VALIDITY = TimeUnit.MINUTES.toMillis(30);


    public Mono<String> generateToken(Mono<UserDetails> userDetailsMono) {
        return userDetailsMono.flatMap(userDetails -> Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", "TTArena_v0");
            claims.put("userId", userDetails.getUsername());
            claims.put("roles",
                    userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

            return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(VALIDITY))).signWith(generateKey()).compact();
        }));
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


    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(decodedKey);
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

        return Mono.fromCallable(() -> {
            Claims claims = getClaims(cleanToken);
            return claims.get("userId", String.class);
        });
    }


    public Mono<List<String>> extractUserRoles(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = getClaims(token);
                Object rolesObject = claims.get("roles"); // Get roles as raw Object

                if (rolesObject instanceof List<?>) {
                    return ((List<?>) rolesObject).stream()
                            .map(Object::toString) // Ensure casting to String
                            .toList();
                } else {
                    throw new IllegalArgumentException("Roles claim is not a valid list");
                }
            } catch (JwtException | IllegalArgumentException e) {
                throw new JwtException(("Error extracting roles from token"), e);
            }
        });
    }


    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }


    public Mono<Boolean> isTokenValid(String jwt) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = getClaims(jwt);
                Date expiration = claims.getExpiration();
                return expiration != null && expiration.after(Date.from(Instant.now()));
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        });
    }

}
