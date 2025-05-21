package org.ttarena.arena_user.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@Document(collection = "arena_users")
public class ArenaUserDocument {

	@Id
	private UUID userId;
	private String username;
	private String password;
	private String email;
	private String role;

	@Builder
	public ArenaUserDocument(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = "USER";
	}


}