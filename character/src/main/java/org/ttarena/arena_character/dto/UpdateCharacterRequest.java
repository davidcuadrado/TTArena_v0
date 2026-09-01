package org.ttarena.arena_character.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ttarena.arena_character.model.Character;

public record UpdateCharacterRequest(

        @NotBlank(message = "name is required")
        @Size(max = Character.NAME_MAX_LENGTH, message = "name must be at most 32 characters")
        @Pattern(regexp = Character.NAME_PATTERN, message = "name must contain only letters")
        String name) {
}
