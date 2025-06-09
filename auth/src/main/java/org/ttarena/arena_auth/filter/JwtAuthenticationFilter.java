package org.ttarena.arena_auth.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.ttarena.arena_auth.service.JwtService;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

        return Mono.justOrEmpty(token)
                .flatMap(jwtService::isTokenValid)
                .filter(valid -> valid)
                .flatMap(valid -> jwtService.extractUsername(token)
                        .zipWith(jwtService.extractUserRoles(token)))
                .map(tuple -> {
                    String username = tuple.getT1();
                    List<GrantedAuthority> authorities = tuple.getT2().stream()
                            .map(role -> new SimpleGrantedAuthority(role.toString()))
                            .collect(Collectors.toList());


                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    return new SecurityContextImpl(auth);
                })
                .flatMap(securityContext ->
                        chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/register") || path.startsWith("/authenticate") ||
                path.startsWith("/home/login") || path.startsWith("/swagger");
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}