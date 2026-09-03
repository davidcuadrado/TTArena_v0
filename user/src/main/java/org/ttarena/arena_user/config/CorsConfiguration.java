package org.ttarena.arena_user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

/**
 * Registration is called straight from a browser, so it needs CORS or the
 * request never leaves the page.
 *
 * <p>Configured at the WebFlux level rather than inside the security chain, so
 * there is exactly one place doing this. Two layers both adding CORS headers
 * produces duplicate {@code Access-Control-Allow-Origin} values, which browsers
 * reject outright.
 *
 * <p>The origins come from {@code ttarena.cors.allowed-origins}, the same
 * property {@code auth} reads, so one environment variable moves both services.
 * They used to be a literal {@code http://localhost:3000} compiled into the
 * security chain, which meant a deployed front end could not call this service
 * at all without a rebuild.
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
