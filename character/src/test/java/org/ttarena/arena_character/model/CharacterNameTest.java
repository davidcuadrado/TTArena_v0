package org.ttarena.arena_character.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"Conan", "Conán", "Ñurdin", "Ragnarök"})
    void acceptsLettersFromAnyAlphabet(String name) {
        assertThatCode(() -> new Warrior(name, 200, 100, WarriorSpecialization.ARMS))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Conan99", "Conan the Barbarian", "Conan!", "Con-an", "", " "})
    void theConstructorRejectsAnythingElse(String name) {
        assertThatThrownBy(() -> new Warrior(name, 200, 100, WarriorSpecialization.ARMS))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("letters only");
    }

    @Test
    void theConstructorRejectsNull() {
        assertThatThrownBy(() -> new Warrior(null, 200, 100, WarriorSpecialization.ARMS))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void namesLongerThanTheLimitAreRejected() {
        String tooLong = "a".repeat(Character.NAME_MAX_LENGTH + 1);

        assertThatThrownBy(() -> new Warrior(tooLong, 200, 100, WarriorSpecialization.ARMS))
                .isInstanceOf(BadRequestException.class);

        assertThatCode(() -> new Warrior("a".repeat(Character.NAME_MAX_LENGTH), 200, 100,
                WarriorSpecialization.ARMS)).doesNotThrowAnyException();
    }

    /**
     * The rule lives on the setter too, so the update endpoint and any internal
     * caller go through it rather than only the request DTO.
     */
    @Test
    void renamingIsCheckedAsWell() {
        Warrior conan = new Warrior("Conan", 200, 100, WarriorSpecialization.ARMS);

        assertThatThrownBy(() -> conan.setName("Conan the Barbarian"))
                .isInstanceOf(BadRequestException.class);

        assertThat(conan.getName()).isEqualTo("Conan");

        conan.setName("Kanan");
        assertThat(conan.getName()).isEqualTo("Kanan");
    }
}
