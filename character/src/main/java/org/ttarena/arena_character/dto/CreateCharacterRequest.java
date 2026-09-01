package org.ttarena.arena_character.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.ttarena.arena_character.model.enums.CharacterClass;

/**
 * Request payload for creating a character of any class.
 *
 * <p>{@code specialization} is a plain String because each class has its own
 * specialization enum; the factory for the requested class is what knows how to
 * parse it, and rejects an unknown value with a 400.
 *
 * @param resourceAmount the class's power resource (rage, mana, energy, ...) -
 *                       which resource it is follows from the class itself.
 */
public record CreateCharacterRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "characterClass is required")
        CharacterClass characterClass,

        @Positive(message = "health must be greater than 0")
        int health,

        @PositiveOrZero(message = "resourceAmount cannot be negative")
        int resourceAmount,

        @NotBlank(message = "specialization is required")
        String specialization) {
}
