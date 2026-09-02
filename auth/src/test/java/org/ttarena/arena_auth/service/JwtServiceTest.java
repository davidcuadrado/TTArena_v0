package org.ttarena.arena_auth.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.ttarena.arena_auth.config.JwtProperties;
import org.ttarena.arena_auth.dto.AuthenticatedUser;
import org.ttarena.arena_auth.security.AuthenticatedUserPrincipal;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The claims here are a contract three other services read, so they are pinned.
 */
class JwtServiceTest {

    private static final AuthenticatedUser ALICE =
            new AuthenticatedUser("11111111-2222-3333-4444-555555555555", "alice", "alice@ttarena.org",
                    List.of("USER"));

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws IOException {
        jwtService = newService(30);
    }

    private JwtService newService(long ttlMinutes) throws IOException {
        return new JwtService(new JwtProperties(new ClassPathResource("keys/dev-private.pem"),
                new ClassPathResource("keys/dev-public.pem"), ttlMinutes));
    }

    private String tokenForAlice() {
        return jwtService.generateToken(AuthenticatedUserPrincipal.of(ALICE)).block();
    }

    @Test
    void theSubjectIsTheUsernameAndUserIdIsTheAccountUuid() {
        String token = tokenForAlice();

        assertThat(token).isNotNull();
        StepVerifier.create(jwtService.extractUsername(token)).expectNext("alice").verifyComplete();
        StepVerifier.create(jwtService.extractUserId(token))
                .expectNext("11111111-2222-3333-4444-555555555555")
                .verifyComplete();
    }

    @Test
    void rolesAreCarriedWithTheRolePrefix() {
        StepVerifier.create(jwtService.extractUserRoles(tokenForAlice()))
                .expectNext(List.of("ROLE_USER"))
                .verifyComplete();
    }

    @Test
    void aFreshTokenIsValid() {
        StepVerifier.create(jwtService.isTokenValid(tokenForAlice())).expectNext(true).verifyComplete();
    }

    @Test
    void theBearerPrefixIsAccepted() {
        StepVerifier.create(jwtService.extractUsername("Bearer " + tokenForAlice()))
                .expectNext("alice")
                .verifyComplete();
    }

    @Test
    void anExpiredTokenIsRejected() throws IOException {
        String expired = newService(-1).generateToken(AuthenticatedUserPrincipal.of(ALICE)).block();

        StepVerifier.create(jwtService.isTokenValid(expired)).expectNext(false).verifyComplete();
    }

    @Test
    void aTamperedTokenIsRejected() {
        String token = tokenForAlice();
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        StepVerifier.create(jwtService.isTokenValid(tampered)).expectNext(false).verifyComplete();
    }

    /**
     * Signed with our key, but minted by something that is not this service.
     */
    @Test
    void aTokenFromAnotherIssuerIsRejected() throws IOException {
        String foreign;
        try (InputStream in = new ClassPathResource("keys/dev-private.pem").getInputStream()) {
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(in);
            foreign = Jwts.builder()
                    .issuer("somebody-else")
                    .subject("alice")
                    .expiration(Date.from(Instant.now().plusSeconds(600)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        }

        StepVerifier.create(jwtService.isTokenValid(foreign)).expectNext(false).verifyComplete();
    }

    @Test
    void garbageIsReportedAsAnInvalidToken() {
        StepVerifier.create(jwtService.validateAndExtractUsername("not-a-token"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void aMissingTokenIsReportedRatherThanThrown() {
        StepVerifier.create(jwtService.isTokenValid(null)).expectNext(false).verifyComplete();
    }

    @Test
    void theConfiguredTtlIsWhatExpires() throws IOException {
        Instant before = Instant.now();
        String token = newService(5).generateToken(AuthenticatedUserPrincipal.of(ALICE)).block();

        Date expiration = Jwts.parser()
                .verifyWith(publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        assertThat(expiration.toInstant())
                .isBetween(before.plus(Duration.ofMinutes(5)).minusSeconds(30),
                        before.plus(Duration.ofMinutes(5)).plusSeconds(30));
    }

    /**
     * Tampering is one thing; a token minted with a completely different key is
     * the case that matters once the dev keypair is replaced per environment.
     */
    @Test
    void aTokenSignedByADifferentKeyIsRejected() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair foreignPair = generator.generateKeyPair();

        String foreign = Jwts.builder()
                .issuer(JwtService.ISSUER)
                .subject("alice")
                .claim(JwtService.USER_ID_CLAIM, "user-1")
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(foreignPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        StepVerifier.create(jwtService.isTokenValid(foreign)).expectNext(false).verifyComplete();
    }

    @Test
    void aTokenWithoutTheUserIdClaimReadsAsNullRatherThanThrowing() throws IOException {
        String withoutUserId;
        try (InputStream in = new ClassPathResource("keys/dev-private.pem").getInputStream()) {
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(in);
            withoutUserId = Jwts.builder()
                    .issuer(JwtService.ISSUER)
                    .subject("alice")
                    .expiration(Date.from(Instant.now().plusSeconds(600)))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        }

        StepVerifier.create(jwtService.extractUserId(withoutUserId)).expectComplete().verify();
    }

    private RSAPublicKey publicKey() throws IOException {
        try (InputStream in = new ClassPathResource("keys/dev-public.pem").getInputStream()) {
            return RsaKeyConverters.x509().convert(in);
        }
    }
}
