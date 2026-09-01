package org.ttarena.arena_game.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_game.dto.CastRequest;
import org.ttarena.arena_game.dto.GameSessionResponse;
import org.ttarena.arena_game.service.GameSessionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameSessionService gameSessionService;

    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> myActiveGame(@AuthenticationPrincipal Jwt jwt) {
        String userId = userId(jwt);
        return gameSessionService.activeSessionOf(userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<GameSessionResponse> myGames(@AuthenticationPrincipal Jwt jwt) {
        String userId = userId(jwt);
        return gameSessionService.sessionsOf(userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> game(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = userId(jwt);
        return gameSessionService.sessionFor(id, userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @PostMapping(value = "/{id}/cast", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> cast(@AuthenticationPrincipal Jwt jwt,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                          @PathVariable String id,
                                          @Valid @RequestBody CastRequest request) {
        String userId = userId(jwt);
        return gameSessionService.cast(id, userId, authorization, request.abilityId())
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @PostMapping(value = "/{id}/surrender", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> surrender(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = userId(jwt);
        return gameSessionService.surrender(id, userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @PostMapping(value = "/{id}/claim-timeout", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> claimTimeout(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = userId(jwt);
        return gameSessionService.claimTimeoutWin(id, userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    @PostMapping(value = "/{id}/rematch", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameSessionResponse> rematch(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = userId(jwt);
        return gameSessionService.rematch(id, userId)
                .map(session -> GameSessionResponse.of(session, userId));
    }

    private static String userId(Jwt jwt) {
        return jwt.getClaimAsString("userId");
    }
}
