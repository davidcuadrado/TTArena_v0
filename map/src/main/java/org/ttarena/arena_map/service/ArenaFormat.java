package org.ttarena.arena_map.service;

import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.dto.ArenaDocument;
import org.ttarena.arena_map.exception.BadRequestException;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Turns an authored {@link ArenaDocument} into tiles, and tiles back into one. */
public final class ArenaFormat {

    private static final int MAX_ELEVATION = 100;

    private ArenaFormat() {
    }

    public static List<HexTile> tilesOf(ArenaDocument arena) {
        if (arena == null) {
            throw new BadRequestException("Arena: nothing to read.");
        }
        if (arena.radius() < 0) {
            throw new BadRequestException("Arena: radius must not be negative, got %d.".formatted(arena.radius()));
        }

        Map<Character, TerrainType> legend = legendOf(arena);
        List<String> grid = arena.grid() == null ? List.of() : arena.grid();
        List<List<Integer>> elevations = arena.elevations() == null ? List.of() : arena.elevations();

        int expectedRows = 2 * arena.radius() + 1;
        if (grid.size() != expectedRows) {
            throw new BadRequestException("Arena: a radius %d arena needs %d grid rows, found %d."
                    .formatted(arena.radius(), expectedRows, grid.size()));
        }
        if (!elevations.isEmpty() && elevations.size() != expectedRows) {
            throw new BadRequestException("Arena: a radius %d arena needs %d elevation rows, found %d."
                    .formatted(arena.radius(), expectedRows, elevations.size()));
        }

        List<HexTile> tiles = new ArrayList<>();
        for (int i = 0; i < expectedRows; i++) {
            int r = i - arena.radius();
            List<HexCoordinate> coordinates = rowOf(arena.radius(), r);
            char[] cells = cellsOf(grid.get(i), i, r, coordinates.size());
            List<Integer> heights = elevations.isEmpty() ? null : heightsOf(elevations.get(i), i, r, coordinates.size());

            for (int c = 0; c < coordinates.size(); c++) {
                TerrainType terrain = legend.get(cells[c]);
                if (terrain == null) {
                    throw new BadRequestException("Arena: grid row %d uses '%c', which the legend does not define."
                            .formatted(i + 1, cells[c]));
                }
                tiles.add(new HexTile(coordinates.get(c), terrain, heights == null ? 0 : heights.get(c)));
            }
        }
        return tiles;
    }

    public static ArenaDocument render(GameMap map) {
        Map<String, String> legend = new LinkedHashMap<>();
        for (TerrainType terrain : TerrainType.values()) {
            if (map.allTiles().stream().anyMatch(tile -> tile.terrain() == terrain)) {
                legend.put(String.valueOf(terrain.symbol()), terrain.name());
            }
        }
        if (legend.isEmpty()) {
            legend.put(String.valueOf(TerrainType.PLAIN.symbol()), TerrainType.PLAIN.name());
        }

        List<String> grid = new ArrayList<>();
        List<List<Integer>> elevations = new ArrayList<>();
        boolean anyElevation = map.allTiles().stream().anyMatch(tile -> tile.elevation() != 0);

        for (int r = -map.getRadius(); r <= map.getRadius(); r++) {
            List<HexCoordinate> coordinates = rowOf(map.getRadius(), r);
            StringBuilder row = new StringBuilder();
            List<Integer> heights = new ArrayList<>(coordinates.size());
            for (int c = 0; c < coordinates.size(); c++) {
                HexTile tile = map.tileAt(coordinates.get(c));
                row.append(c == 0 ? "" : " ")
                        .append(tile == null ? TerrainType.PLAIN.symbol() : tile.terrain().symbol());
                heights.add(tile == null ? 0 : tile.elevation());
            }
            grid.add(row.toString());
            elevations.add(heights);
        }

        return new ArenaDocument(map.getName(), map.getDescription(), map.getRadius(), legend, grid,
                anyElevation ? elevations : null);
    }

    public static List<HexCoordinate> rowOf(int radius, int r) {
        int firstQ = Math.max(-radius, -radius - r);
        int width = 2 * radius + 1 - Math.abs(r);
        List<HexCoordinate> row = new ArrayList<>(width);
        for (int q = firstQ; q < firstQ + width; q++) {
            row.add(new HexCoordinate(q, r, -q - r));
        }
        return row;
    }

    private static Map<Character, TerrainType> legendOf(ArenaDocument arena) {
        if (arena.legend() == null || arena.legend().isEmpty()) {
            throw new BadRequestException("Arena: no legend.");
        }

        Map<Character, TerrainType> legend = new HashMap<>();
        arena.legend().forEach((symbol, terrain) -> {
            if (symbol == null || symbol.length() != 1) {
                throw new BadRequestException(
                        "Arena: legend key '%s' must be a single character.".formatted(symbol));
            }
            legend.put(symbol.charAt(0), TerrainType.named(terrain));
        });
        return legend;
    }

    private static char[] cellsOf(String row, int index, int r, int expected) {
        char[] cells = (row == null ? "" : row.replaceAll("\\s+", "")).toCharArray();
        if (cells.length != expected) {
            throw new BadRequestException("Arena: grid row %d (r=%d) has %d cells, expected %d."
                    .formatted(index + 1, r, cells.length, expected));
        }
        return cells;
    }

    private static List<Integer> heightsOf(List<Integer> row, int index, int r, int expected) {
        if (row == null || row.size() != expected) {
            throw new BadRequestException("Arena: elevation row %d (r=%d) has %d cells, expected %d."
                    .formatted(index + 1, r, row == null ? 0 : row.size(), expected));
        }
        row.forEach(elevation -> {
            if (elevation == null || elevation < 0 || elevation > MAX_ELEVATION) {
                throw new BadRequestException("Arena: elevation %s in row %d is outside 0..%d."
                        .formatted(elevation, index + 1, MAX_ELEVATION));
            }
        });
        return row;
    }
}
