package org.ttarena.arena_character.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ttarena.arena_character.model.enums.CharacterClass;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCharacterRequestValidationTest {

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

    private Set<String> nameViolations(String name) {
        return validator.validate(
                        new CreateCharacterRequest(name, CharacterClass.WARRIOR, 200, 100, "ARMS")).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("name"))
                .map(violation -> violation.getMessage())
                .collect(Collectors.toSet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Conan", "conan", "CONAN", "Alexstrasza"})
    void acceptsLettersOnly(String name) {
        assertThat(nameViolations(name)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Conan99", "Conan the Barbarian", "Conan!", "Con-an", "Con_an", "Conán", "123"})
    void rejectsDigitsPunctuationAndSpaces(String name) {
        assertThat(nameViolations(name)).contains("name must contain only letters");
    }
}
