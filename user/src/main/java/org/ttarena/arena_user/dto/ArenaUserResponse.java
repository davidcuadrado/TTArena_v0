package org.ttarena.arena_user.dto;

import org.ttarena.arena_user.document.ArenaUserDocument;

import java.util.List;

public record ArenaUserResponse(String userId, String username, String email, List<String> roles) {

	public static ArenaUserResponse from(ArenaUserDocument user) {
		String role = user.getRole() == null ? "USER" : user.getRole();
		return new ArenaUserResponse(
				user.getUserId(),
				user.getUsername(),
				user.getEmail(),
				List.of(role.split(",")));
	}
}
