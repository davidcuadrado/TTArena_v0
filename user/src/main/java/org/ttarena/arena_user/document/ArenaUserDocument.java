package org.ttarena.arena_user.document;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.exception.BadRequestException;

import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "arena_users")
public class ArenaUserDocument {

	public static final String USERNAME_PATTERN = "^[\\p{L}\\p{N}]+$";
	public static final int USERNAME_MIN_LENGTH = 3;
	public static final int USERNAME_MAX_LENGTH = 32;

	@Id
	private String userId;

	@Setter(lombok.AccessLevel.NONE)
	@AccessType(AccessType.Type.FIELD)
	@Indexed(unique = true)
	private String username;

	private String password;
	private String email;
	private String role;

	@Builder
	public ArenaUserDocument(String userId, String username, String password, String email, String role) {
		this.userId = userId;
		this.username = requireValidUsername(username);
		this.password = password;
		this.email = email;
		this.role = role;
	}

	public static ArenaUserDocument newUser(String username, String email, String encodedPassword) {
		return ArenaUserDocument.builder()
				.userId(UUID.randomUUID().toString())
				.username(username)
				.email(email)
				.password(encodedPassword)
				.role("USER")
				.build();
	}

	public void setUsername(String username) {
		this.username = requireValidUsername(username);
	}

	private static String requireValidUsername(String username) {
		if (username == null || !username.matches(USERNAME_PATTERN)
				|| username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
			throw new BadRequestException("Invalid username '" + username + "': letters and numbers only, "
					+ USERNAME_MIN_LENGTH + " to " + USERNAME_MAX_LENGTH + " characters.");
		}
		return username;
	}
}
