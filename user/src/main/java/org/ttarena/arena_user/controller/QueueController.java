package org.ttarena.arena_user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_user.dto.JoinQueueRequest;
import org.ttarena.arena_user.dto.QueueStatusResponse;
import org.ttarena.arena_user.security.ArenaUserPrincipal;
import org.ttarena.arena_user.service.MatchmakingQueueService;
import reactor.core.publisher.Mono;

/**
 * Joining and leaving the matchmaking queue.
 *
 * <p>These publish the status events the matchmaking service subscribes to.
 * Queueing is deliberately explicit rather than tied to logging in: being signed
 * in is presence, asking for a match is intent, and they are not the same thing.
 */
@Tag(name = "Matchmaking queue", description = "join and leave the queue")
@RestController
@RequestMapping("/user/queue")
public class QueueController {

	private final MatchmakingQueueService matchmakingQueueService;

	public QueueController(MatchmakingQueueService matchmakingQueueService) {
		this.matchmakingQueueService = matchmakingQueueService;
	}

	@Operation(summary = "Join the queue",
			description = "Publishes USER_CONNECTED for the signed-in account and the character it will play.")
	@PostMapping("/join")
	public Mono<QueueStatusResponse> join(@AuthenticationPrincipal ArenaUserPrincipal principal,
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@Valid @RequestBody JoinQueueRequest request) {
		return matchmakingQueueService.join(authorization, principal.getUserId(), request.characterId())
				.map(listeners -> QueueStatusResponse.queued(principal.getUserId(), request.characterId(), listeners));
	}

	@Operation(summary = "Leave the queue", description = "Publishes USER_DISCONNECTED for the signed-in account.")
	@PostMapping("/leave")
	public Mono<QueueStatusResponse> leave(@AuthenticationPrincipal ArenaUserPrincipal principal) {
		return matchmakingQueueService.leave(principal.getUserId())
				.map(listeners -> QueueStatusResponse.left(principal.getUserId(), listeners));
	}
}
