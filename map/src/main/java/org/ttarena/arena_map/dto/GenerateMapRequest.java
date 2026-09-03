package org.ttarena.arena_map.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ttarena.arena_map.model.MapName;
import org.ttarena.arena_map.model.TerrainType;

/** Creates a blank canvas of one terrain to hand-author on top of. */
public record GenerateMapRequest(
        @NotBlank
        @Size(max = MapName.MAX_LENGTH)
        @Pattern(regexp = MapName.PATTERN, message = MapName.MESSAGE)
        String name,

        @Size(max = 512)
        String description,

        @Min(0)
        int radius,

        @NotNull TerrainType terrain) {
}
