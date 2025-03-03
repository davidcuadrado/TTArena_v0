package org.ttarena.arena_user.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;


@Data
@Builder
@Document(collection = "user")
public class ArenaUserDocument {

	@Id
	private UUID userId;
	private String username;
	private String password;
	private String role;

	public ArenaUserDocument(String username, String password) {
		this.username = username;
		this.password = password;
		this.role = "USER";
	}

	
}