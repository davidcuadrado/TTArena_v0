package org.ttarena.arena_map.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ttarena.arena_map.model.MapName;

public record CreateMapRequest(
        @NotBlank
        @Size(max = MapName.MAX_LENGTH)
        @Pattern(regexp = MapName.PATTERN, message = MapName.MESSAGE)
        String name,

        @Size(max = 512)
        String description) {
}
