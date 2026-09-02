package org.ttarena.arena_user.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ttarena.arena_user.exception.BadRequestException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArenaUserDocumentTest {

	@ParameterizedTest
	@ValueSource(strings = {"conan", "Conan99", "99conan", "conán", "Ñuria"})
	void acceptsAlphanumericUsernames(String username) {
		assertThatCode(() -> ArenaUserDocument.newUser(username, "player@ttarena.org", "hashed"))
				.doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = {"con an", "conan!", "con-an", "con_an", "<script>", "ab"})
	void rejectsAnythingElse(String username) {
		assertThatThrownBy(() -> ArenaUserDocument.newUser(username, "player@ttarena.org", "hashed"))
				.isInstanceOf(BadRequestException.class);
	}

	/**
	 * The builder sets fields directly, so it has to route through the same check
	 * as the setter - otherwise it would be a way around the rule.
	 */
	@Test
	void theBuilderCannotBypassTheRule() {
		assertThatThrownBy(() -> ArenaUserDocument.builder()
				.userId("some-id")
				.username("con an")
				.password("hashed")
				.email("player@ttarena.org")
				.role("USER")
				.build())
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void renamingIsCheckedAsWell() {
		ArenaUserDocument user = ArenaUserDocument.newUser("conan", "player@ttarena.org", "hashed");

		assertThatThrownBy(() -> user.setUsername("con an")).isInstanceOf(BadRequestException.class);
		assertThat(user.getUsername()).isEqualTo("conan");

		user.setUsername("conan2");
		assertThat(user.getUsername()).isEqualTo("conan2");
	}

	@Test
	void assignsAUuidAndTheDefaultRole() {
		ArenaUserDocument user = ArenaUserDocument.newUser("conan", "player@ttarena.org", "hashed");

		assertThat(user.getUserId()).isNotNull();
		assertThatCode(() -> UUID.fromString(user.getUserId())).doesNotThrowAnyException();
		assertThat(user.getRole()).isEqualTo("USER");
		assertThat(user.getPassword()).isEqualTo("hashed");
	}
}
