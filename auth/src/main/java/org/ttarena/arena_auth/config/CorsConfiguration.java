package org.ttarena.arena_auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

/**
 * The login endpoint is the first thing a browser calls, so it needs CORS or the
 * request never leaves the page.
 *
 * <p>Configured at the WebFlux level rather than inside the security chain, so
 * there is exactly one place doing this. Two layers both adding CORS headers
 * produces duplicate {@code Access-Control-Allow-Origin} values, which browsers
 * reject outright.
 */
@Slf4j
@Configuration
public class CorsConfiguration implements WebFluxConfigurer {

    private final List<String> allowedOrigins;

    public CorsConfiguration(
            @Value("${ttarena.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        log.info("CORS allows origins: {}", allowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
