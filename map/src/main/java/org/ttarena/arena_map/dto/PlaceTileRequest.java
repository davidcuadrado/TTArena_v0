package org.ttarena.arena_map.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.ttarena.arena_map.model.TerrainType;

public record PlaceTileRequest(
        @NotNull TerrainType terrain,
        @Min(0) @Max(100) int elevation) {
}
