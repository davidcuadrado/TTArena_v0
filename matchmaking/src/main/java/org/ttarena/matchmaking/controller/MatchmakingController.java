package org.ttarena.matchmaking.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.matchmaking.dto.MatchmakingStatusResponse;
import org.ttarena.matchmaking.service.MatchmakingService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @GetMapping("/me")
    public Mono<MatchmakingStatusResponse> myStatus(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");

        return Mono.just(MatchmakingStatusResponse.of(
                userId,
                matchmakingService.isQueued(userId),
                matchmakingService.queueSize(),
                matchmakingService.lastMatchOf(userId).orElse(null)));
    }
}
