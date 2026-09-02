package org.ttarena.arena_auth.config;

/**
 * Paths reachable without a token. Shared by the security chain and the JWT
 * filter so the two cannot disagree - they did, and login was unreachable.
 */
public final class PublicPaths {

    public static final String[] ALL = {
            "/auth/login",
            "/actuator/health/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private PublicPaths() {
    }

    public static boolean matches(String path) {
        return path.equals("/auth/login")
                || path.startsWith("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger");
    }
}
