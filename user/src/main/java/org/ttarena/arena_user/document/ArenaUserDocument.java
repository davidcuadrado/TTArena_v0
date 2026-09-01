package org.ttarena.arena_user.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "arena_users")
public class ArenaUserDocument {

	@Id
	private String userId;

	@Indexed(unique = true)
	private String username;

	private String password;
	private String email;
	private String role;

	public static ArenaUserDocument newUser(String username, String email, String encodedPassword) {
		return ArenaUserDocument.builder()
				.userId(UUID.randomUUID().toString())
				.username(username)
				.email(email)
				.password(encodedPassword)
				.role("USER")
				.build();
	}
}
