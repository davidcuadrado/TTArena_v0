package org.ttarena.arena_user.service;

import org.springframework.stereotype.Service;
import org.ttarena.arena_user.client.CharacterServiceClient;
import org.ttarena.arena_user.exception.BadRequestException;
import org.ttarena.arena_user.util.UserEventType;
import reactor.core.publisher.Mono;

/**
 * Joining and leaving the matchmaking queue.
 *
 * <p>The character is checked against the caller's roster first: matchmaking has
 * no way to verify it, and a match made with a character the player does not own
 * produces a session nobody can play.
 */
@Service
public class MatchmakingQueueService {

	private final CharacterServiceClient characterService;
	private final RedisPublisherService redisPublisherService;

	public MatchmakingQueueService(CharacterServiceClient characterService,
			RedisPublisherService redisPublisherService) {
		this.characterService = characterService;
		this.redisPublisherService = redisPublisherService;
	}

	public Mono<Long> join(String bearerToken, String userId, String characterId) {
		return characterService.ownsCharacter(bearerToken, characterId)
				.flatMap(owned -> Boolean.TRUE.equals(owned)
						? redisPublisherService.publishUserEvent(UserEventType.USER_CONNECTED, userId, characterId)
						: Mono.<Long>error(new BadRequestException(
								"Character " + characterId + " is not on your account.")));
	}

	public Mono<Long> leave(String userId) {
		return redisPublisherService.publishUserEvent(UserEventType.USER_DISCONNECTED, userId, null);
	}
}
