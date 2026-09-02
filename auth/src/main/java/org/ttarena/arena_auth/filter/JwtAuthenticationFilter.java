package org.ttarena.arena_auth.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.ttarena.arena_auth.service.JwtService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Populates the security context from a bearer token.
 *
 * <p>Deliberately not a {@code @Component}: a WebFilter bean is applied globally
 * by WebFlux, which would run it a second time outside the security chain. It is
 * constructed by {@code SecurityConfiguration} and registered there instead.
 *
 * <p>It never terminates the exchange. A missing or unreadable token leaves the
 * request unauthenticated and lets the authorization filter answer, so one
 * component decides what a rejection looks like.
 */
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
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
        return jwtService.extractUsername(token)
                .zipWith(jwtService.extractUserRoles(token))
                .map(tuple -> {
                    List<GrantedAuthority> authorities = tuple.getT2().stream()
                            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                            .toList();

                    return new SecurityContextImpl(
                            new UsernamePasswordAuthenticationToken(tuple.getT1(), null, authorities));
                });
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
