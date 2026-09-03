package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.ttarena.arena_user.service.ArenaUserService;
import org.ttarena.arena_user.service.JwtService;
import reactor.core.publisher.Mono;

/**
 * Who may call what.
 *
 * <p>{@link PublicPaths} is open, everything under {@code /user} needs the USER
 * role, and anything else needs a token. The chain used to also route
 * {@code /character/**}, {@code /home/**} and {@code /develop/**} - paths left
 * over from a layout where this module served more than accounts, and which no
 * controller here answers. It also listed {@code /user/**} a second time behind
 * an ADMIN role that the first rule made unreachable, and put the springdoc
 * paths behind a DEVELOPER role that registration never grants, so this
 * service's Swagger UI answered 403 to everyone including its owner.
 *
 * <p>CORS is handled by {@link CorsConfiguration} at the WebFlux level, not
 * here: two layers adding the headers produces duplicates that browsers reject.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

	private final ArenaUserService arenaUserService;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(ArenaUserService arenaUserService, JwtService jwtService) {
		this.arenaUserService = arenaUserService;
		// Built here rather than injected: as a bean it would also be picked up
		// as a global WebFilter and run twice.
		this.jwtAuthenticationFilter = new JwtAuthenticationFilter(arenaUserService, jwtService);
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.authorizeExchange(exchanges -> exchanges
						// Preflights carry no credentials by definition, so they
						// must never be subjected to authorization.
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers(PublicPaths.ALL).permitAll()
						.pathMatchers("/user/**").hasRole("USER")
						.anyExchange().authenticated())
				.exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
						.authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(() ->
							exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
						)).accessDeniedHandler((exchange, denied) -> Mono.fromRunnable(() ->
							exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)
						)))
				// Registered inside the chain: a plain WebFilter bean runs after
				// Spring Security has already rejected the request, so it could
				// never authenticate anything.
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.build();
	}

	@Bean
	public ReactiveUserDetailsService userDetailsService() {
		return arenaUserService;
	}

	@Bean
	public ReactiveAuthenticationManager authenticationManager() {
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService());
	}
}
