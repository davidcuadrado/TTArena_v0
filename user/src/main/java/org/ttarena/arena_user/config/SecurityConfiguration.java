package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.ttarena.arena_user.service.ArenaUserService;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

	private final ArenaUserService arenaUserService;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, ArenaUserService arenaUserService) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.arenaUserService = arenaUserService;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/authenticate/**", "home/login", "/home/register", "home/authenticate",
								"/register/**")
						.permitAll().pathMatchers("/user/**", "/character/**").hasRole("USER")
						.pathMatchers("/admin/**", "/user/**", "/character/**").hasRole("ADMIN")
						.pathMatchers("/develop/**", "/develop-ability/**", "/develop-class/**", "/develop-map/**",
								"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.hasRole("DEVELOPER").anyExchange().authenticated())
				.exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
						.authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(() ->
							exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
						)).accessDeniedHandler((exchange, denied) -> Mono.fromRunnable(() ->
							exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)
						)))
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("http://localhost:3000");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/*
	 * CORS Allowed methods: config.setAllowCredentials(true);
	 * config.addAllowedMethod("GET"); config.addAllowedMethod("POST");
	 * config.addAllowedMethod("PUT"); config.addAllowedMethod("DELETE");
	 * config.addAllowedHeader("*");
	 * 
	 * UrlBasedCorsConfigurationSource source = new
	 * UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",
	 * config); return new CorsWebFilter(source); }
	 */

	@Bean
	public ReactiveUserDetailsService userDetailsService() {
		return arenaUserService;
	}

	@Bean
	public ReactiveAuthenticationManager authenticationManager() {
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
