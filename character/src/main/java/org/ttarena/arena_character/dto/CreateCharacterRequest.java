package org.ttarena.arena_character.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.ttarena.arena_character.model.enums.CharacterClass;

public record CreateCharacterRequest(

        @NotBlank(message = "name is required")
        @Size(max = 32, message = "name must be at most 32 characters")
        @Pattern(regexp = "^\\p{L}+$", message = "name must contain only letters")
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
