package org.ttarena.arena_user.config;

/**
 * Paths reachable without a token, written down once.
 *
 * <p>The security chain permits them and {@link JwtAuthenticationFilter} skips
 * them. The two lists used to be maintained separately and had drifted: the
 * chain permitted {@code /home/login} and {@code /register/**} that no
 * controller serves, the filter skipped {@code /swagger} that the chain put
 * behind a role nobody holds, and neither of them mentioned the actuator, so
 * this was the one service whose health endpoint needed a token.
 */
public final class PublicPaths {

	public static final String[] ALL = {
			"/user/register",
			"/users/authenticate",
			"/actuator/health/**",
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html"
	};

	private PublicPaths() {
	}

	public static boolean matches(String path) {
		return path.equals("/user/register")
				|| path.equals("/users/authenticate")
				|| path.startsWith("/actuator/health")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/swagger");
	}
}
