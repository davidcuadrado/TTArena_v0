package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;
import org.ttarena.arena_user.service.JwtService;

@Configuration
public class JwtAuthenticationFilter implements WebFilter {

	private final JwtService jwtService;
	private final ArenaUserService arenaUserService;

	public JwtAuthenticationFilter(ArenaUserService arenaUserService, JwtService jwtService) {
		this.arenaUserService = arenaUserService;
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getPath().value();
		if (path.startsWith("/register") || path.startsWith("/authenticate") || path.startsWith("/home/login")
				|| path.startsWith("/swagger")) {
			return chain.filter(exchange);
		}

		String token = extractToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

		return Mono.justOrEmpty(token).flatMap(jwtService::validateAndExtractUsername)
				.flatMap(arenaUserService::findByUsername).map(userDetails -> {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					return new SecurityContextImpl(auth);
				})
				.flatMap(securityContext -> chain.filter(exchange)
						.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))

				.onErrorResume(e -> {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				});

	}

	public String extractToken(String header) {
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		return header.substring(7);
	}
}
