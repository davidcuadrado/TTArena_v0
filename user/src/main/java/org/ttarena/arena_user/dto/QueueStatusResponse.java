package org.ttarena.arena_user.dto;

public record QueueStatusResponse(String userId, String characterId, QueueState state,
		long matchmakingListeners) {

	public enum QueueState {
		QUEUED,
		LEFT
	}

	public static QueueStatusResponse queued(String userId, String characterId, long listeners) {
		return new QueueStatusResponse(userId, characterId, QueueState.QUEUED, listeners);
	}

	public static QueueStatusResponse left(String userId, long listeners) {
		return new QueueStatusResponse(userId, null, QueueState.LEFT, listeners);
	}
}
