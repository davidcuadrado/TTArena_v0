package org.ttarena.arena_user.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.ttarena.arena_user.service.ArenaUserService;
import org.ttarena.arena_user.service.JwtService;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Populates the security context from a bearer token.
 *
 * <p>Deliberately not a {@code @Component} or {@code @Configuration}: a
 * WebFilter bean is applied globally by WebFlux, so it ran once there and again
 * inside the security chain - two token parses and two MongoDB lookups for every
 * authenticated request. {@link SecurityConfiguration} constructs it and
 * registers it in the chain instead, which is the only place it belongs.
 *
 * <p>It never terminates the exchange. A missing or unreadable token leaves the
 * request unauthenticated and lets the authorization filter answer, so one
 * component decides what a rejection looks like. It used to answer 401 itself,
 * and when there was no token at all it returned an empty sequence without
 * calling the chain - which is indistinguishable from a completed response, so
 * the caller got 200 and an empty body instead of a rejection.
 */
public class JwtAuthenticationFilter implements WebFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
	private final ArenaUserService arenaUserService;

	public JwtAuthenticationFilter(ArenaUserService arenaUserService, JwtService jwtService) {
		this.arenaUserService = arenaUserService;
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// Registration and the actuator carry no account, so there is nothing to
		// look up. Skipping them keeps a stale token from costing a database
		// round-trip on a path that does not read the result.
		if (PublicPaths.matches(exchange.getRequest().getPath().value())) {
			return chain.filter(exchange);
		}

		String token = extractToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
		if (token == null) {
			return chain.filter(exchange);
		}

		// Resolved to an Optional first so the chain below runs exactly once.
		// Continuing a Mono<Void> with switchIfEmpty would always fire - a
		// completed chain is an empty sequence - and process the request twice.
		return securityContextFor(token)
				.map(Optional::of)
				.defaultIfEmpty(Optional.empty())
				.onErrorReturn(Optional.empty())
				.flatMap(maybeContext -> maybeContext
						.map(context -> chain.filter(exchange).contextWrite(
								ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))))
						.orElseGet(() -> chain.filter(exchange)));
	}

	private Mono<SecurityContext> securityContextFor(String token) {
		return jwtService.validateAndExtractUsername(token)
				.flatMap(arenaUserService::findByUsername)
				.map(userDetails -> new SecurityContextImpl(
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())));
	}

	private String extractToken(String header) {
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return header.substring(BEARER_PREFIX.length());
	}
}
