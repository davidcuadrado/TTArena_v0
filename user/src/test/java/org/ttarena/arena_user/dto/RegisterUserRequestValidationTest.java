package org.ttarena.arena_user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserRequestValidationTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	private Set<String> usernameViolations(String username) {
		return validator.validate(new RegisterUserRequest(username, "player@ttarena.org", "password123")).stream()
				.filter(violation -> violation.getPropertyPath().toString().equals("username"))
				.map(violation -> violation.getMessage())
				.collect(Collectors.toSet());
	}

	@ParameterizedTest
	@ValueSource(strings = {"Conan", "conan", "CONAN", "conan99", "99conan", "aBc123", "conán", "Ñuria"})
	void acceptsAlphanumericUsernames(String username) {
		assertThat(usernameViolations(username)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"con an", "conan!", "con-an", "con_an", "con.an", "con@an", "<script>", "conan "})
	void rejectsAnythingElse(String username) {
		assertThat(usernameViolations(username))
				.contains("username must contain only letters and numbers");
	}
}
