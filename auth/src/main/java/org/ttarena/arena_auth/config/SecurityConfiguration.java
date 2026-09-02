package org.ttarena.arena_auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.ttarena.arena_auth.filter.JwtAuthenticationFilter;
import org.ttarena.arena_auth.service.JwtService;

/**
 * Without a chain of its own the module would fall back to Spring Boot's
 * default, which authenticates every exchange - including the login endpoint
 * whose whole purpose is to be callable without a token.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtService jwtService) {
        // Built here rather than injected: as a bean it would also be picked up
        // as a global WebFilter and run twice.
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
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
                        .anyExchange().authenticated())
                // Registered inside the chain: a plain WebFilter bean runs after
                // Spring Security has already rejected the request, so it could
                // never authenticate anything.
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
