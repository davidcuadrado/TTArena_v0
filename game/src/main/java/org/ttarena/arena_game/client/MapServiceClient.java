package org.ttarena.arena_game.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ttarena.arena_game.document.HexCoordinate;
import org.ttarena.arena_game.exception.BadRequestException;
import org.ttarena.arena_game.exception.ForbiddenException;
import org.ttarena.arena_game.exception.NotFoundException;
import org.ttarena.arena_game.exception.UpstreamUnavailableException;
import reactor.core.publisher.Mono;

import java.time.Duration;

import java.util.List;

/**
 * Reads the arena from the map service with the player's own token. Only two
 * things are ever asked for - where players start, and what a move costs - so
 * the arena's tiles never have to cross the wire.
 */
@Component
public class MapServiceClient {

    private final WebClient webClient;
    private final Duration responseTimeout;

    public MapServiceClient(WebClient.Builder builder,
                            @Value("${map-service.base-url}") String baseUrl,
                            @Value("${map-service.response-timeout:5s}") Duration responseTimeout) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.responseTimeout = responseTimeout;
    }

    public Mono<List<HexCoordinate>> startingPositions(String bearerToken, String mapId, int howMany) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/maps/{id}/starting-positions")
                        .queryParam("count", howMany)
                        .build(mapId))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToFlux(HexCoordinate.class)
                .collectList()
                .timeout(responseTimeout, Mono.defer(() -> Mono.error(new UpstreamUnavailableException(
                        "The map service did not answer within " + responseTimeout + "."))))
                .onErrorMap(WebClientResponseException.class, MapServiceClient::translate);
    }

    public Mono<PathResponse> path(String bearerToken, String mapId, HexCoordinate from, HexCoordinate to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/maps/{id}/path")
                        .queryParam("from", from.key())
                        .queryParam("to", to.key())
                        .build(mapId))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(PathResponse.class)
                .timeout(responseTimeout, Mono.defer(() -> Mono.error(new UpstreamUnavailableException(
                        "The map service did not answer within " + responseTimeout + "."))))
                .onErrorMap(WebClientResponseException.class, MapServiceClient::translate);
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
}
