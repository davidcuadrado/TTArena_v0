package org.ttarena.arena_map.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ttarena.arena_map.model.MapName;

import java.util.List;
import java.util.Map;

/**
 * An arena as it is authored: a legend and a grid of rows, so the file still
 * looks like the map it describes.
 *
 * <pre>
 * {
 *   "name": "Frozen Pass",
 *   "radius": 2,
 *   "legend": { ".": "PLAIN", "f": "FOREST", "^": "MOUNTAIN", "~": "WATER" },
 *   "grid": ["~ . .", ". f . ^", ". . . f .", ". ^ . .", ". . ~"]
 * }
 * </pre>
 *
 * Rows run from r = -radius to r = +radius and hold {@code 2*radius+1-|r|}
 * cells. Every cell is one legend character; whitespace inside a row is
 * decoration, so "~ . ." and "~.." mean the same thing.
 */
public record ArenaDocument(
        @Size(max = MapName.MAX_LENGTH)
        @Pattern(regexp = MapName.PATTERN, message = MapName.MESSAGE)
        String name,

        @Size(max = 512)
        String description,

        @Min(0)
        int radius,

        @NotEmpty(message = "an arena needs a legend")
        Map<String, String> legend,

        @NotEmpty(message = "an arena needs a grid")
        List<String> grid,

        List<List<Integer>> elevations) {
}
