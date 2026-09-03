package org.ttarena.arena_game.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.exception.UpstreamUnavailableException;
import reactor.core.publisher.Mono;

import java.time.Duration;

import java.util.List;

/**
 * Calls the character service to resolve a cast, forwarding the player's own
 * token. The character service therefore re-checks that the caster belongs to
 * that player: this module decides whose turn it is, it does not decide who
 * owns what.
 */
@Component
public class CharacterServiceClient {

    private final WebClient webClient;
    private final Duration responseTimeout;

    public CharacterServiceClient(WebClient.Builder builder,
                                  @Value("${character-service.base-url}") String baseUrl,
                                @Value("${character-service.response-timeout:5s}") Duration responseTimeout) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.responseTimeout = responseTimeout;
    }

    public Mono<CombatResultResponse> cast(String bearerToken, String casterId, String abilityId,
                                           List<String> targetIds, Integer distanceToTarget) {
        return webClient.post()
                .uri("/api/abilities/cast")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .bodyValue(new CastAbilityPayload(casterId, abilityId, targetIds, distanceToTarget))
                .retrieve()
                .bodyToMono(CombatResultResponse.class)
                .timeout(responseTimeout, Mono.error(new UpstreamUnavailableException(
                        "The character service did not answer within " + responseTimeout + ".")))
                .onErrorMap(WebClientResponseException.class, CharacterServiceClient::translate);
    }

    private static Throwable translate(WebClientResponseException e) {
        String body = e.getResponseBodyAsString();
        String message = body == null || body.isBlank() ? e.getMessage() : body;

        if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            return new ForbiddenException(message);
        }
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new NotFoundException(message);
        }
        if (e.getStatusCode().is4xxClientError()) {
            return new BadRequestException(message);
        }
        return e;
    }

    private record CastAbilityPayload(String casterId, String abilityId, List<String> targetIds,
                                      Integer distanceToTarget) {
    }
}
