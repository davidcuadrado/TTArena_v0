package org.ttarena.arena_user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ttarena.arena_user.exception.BadRequestException;
import reactor.core.publisher.Mono;

/**
 * Asks the character service which characters belong to the caller, so a player
 * cannot queue with a character that is not theirs.
 */
@Component
public class CharacterServiceClient {

	private final WebClient webClient;

	public CharacterServiceClient(WebClient.Builder builder,
			@Value("${character-service.base-url}") String baseUrl) {
		this.webClient = builder.baseUrl(baseUrl).build();
	}

	public Mono<Boolean> ownsCharacter(String bearerToken, String characterId) {
		return webClient.get()
				.uri("/api/characters/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken)
				.retrieve()
				.bodyToFlux(CharacterSummary.class)
				.any(character -> characterId.equals(character.id()))
				.onErrorMap(WebClientResponseException.class,
						e -> new BadRequestException("Could not check your roster: " + e.getStatusCode()));
	}

	private record CharacterSummary(String id) {
	}
}
