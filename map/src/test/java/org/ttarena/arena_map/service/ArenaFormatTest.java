package org.ttarena.arena_map.service;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.dto.ArenaDocument;
import org.ttarena.arena_map.exception.BadRequestException;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArenaFormatTest {

    private static final Map<String, String> LEGEND =
            Map.of(".", "PLAIN", "f", "FOREST", "^", "MOUNTAIN", "~", "WATER");

    private static final List<String> GRID = List.of(
            "~ . .",
            ". f . ^",
            ". . . f .",
            ". ^ . .",
            ". . ~");

    private static ArenaDocument frozenPass() {
        return new ArenaDocument("Frozen Pass", "A narrow icy corridor", 2, LEGEND, GRID, null);
    }

    private static GameMap mapOf(ArenaDocument arena) {
        GameMap map = new GameMap();
        map.setName(arena.name());
        map.setDescription(arena.description());
        map.setRadius(arena.radius());
        ArenaFormat.tilesOf(arena).forEach(map::putTile);
        return map;
    }

    @Test
    void readsEveryCellOfTheGrid() {
        assertThat(ArenaFormat.tilesOf(frozenPass())).hasSize(MapGenerator.tileCountFor(2));
    }

    @Test
    void theFirstRowIsTheTopOfTheGrid() {
        GameMap map = mapOf(frozenPass());

        assertThat(map.tileAt(new HexCoordinate(0, -2, 2)).terrain()).isEqualTo(TerrainType.WATER);
        assertThat(map.tileAt(new HexCoordinate(2, -2, 0)).terrain()).isEqualTo(TerrainType.PLAIN);
    }

    @Test
    void theMiddleRowRunsThroughTheOrigin() {
        GameMap map = mapOf(frozenPass());

        assertThat(map.tileAt(HexCoordinate.origin()).terrain()).isEqualTo(TerrainType.PLAIN);
        assertThat(map.tileAt(new HexCoordinate(1, 0, -1)).terrain()).isEqualTo(TerrainType.FOREST);
    }

    @Test
    void everyCoordinateSitsInsideTheRadius() {
        assertThat(ArenaFormat.tilesOf(frozenPass()))
                .allSatisfy(tile -> assertThat(tile.coordinate().ringIndex()).isLessThanOrEqualTo(2));
    }

    @Test
    void whitespaceInsideARowIsDecoration() {
        ArenaDocument spaced = frozenPass();
        ArenaDocument tight = new ArenaDocument("Frozen Pass", null, 2, LEGEND,
                GRID.stream().map(row -> row.replace(" ", "")).toList(), null);

        assertThat(ArenaFormat.tilesOf(tight)).isEqualTo(ArenaFormat.tilesOf(spaced));
    }

    @Test
    void whatIsRenderedReadsBackAsTheSameArena() {
        GameMap original = mapOf(frozenPass());
        GameMap reparsed = mapOf(ArenaFormat.render(original));

        assertThat(reparsed.tileCount()).isEqualTo(original.tileCount());
        assertThat(original.allTiles()).allSatisfy(tile ->
                assertThat(reparsed.tileAt(tile.coordinate())).isEqualTo(tile));
    }

    @Test
    void renderingIsStableSoAMapDoesNotChurnInGit() {
        ArenaDocument once = ArenaFormat.render(mapOf(frozenPass()));

        assertThat(ArenaFormat.render(mapOf(once))).isEqualTo(once);
    }

    @Test
    void theRenderedLegendCoversOnlyTheTerrainActuallyUsed() {
        ArenaDocument rendered = ArenaFormat.render(mapOf(frozenPass()));

        assertThat(rendered.legend().values())
                .containsExactlyInAnyOrder("PLAIN", "FOREST", "WATER", "MOUNTAIN");
    }

    @Test
    void elevationsAreOptionalAndSurviveTheRoundTrip() {
        ArenaDocument withHeights = new ArenaDocument("Frozen Pass", null, 2, LEGEND, GRID, List.of(
                List.of(0, 1, 2),
                List.of(1, 2, 3, 4),
                List.of(2, 3, 9, 3, 2),
                List.of(1, 2, 3, 4),
                List.of(0, 1, 2)));
        GameMap map = mapOf(withHeights);

        assertThat(map.tileAt(HexCoordinate.origin()).elevation()).isEqualTo(9);
        assertThat(ArenaFormat.render(map).elevations()).isNotNull();
    }

    @Test
    void aFlatArenaRendersWithoutAnElevationBlock() {
        assertThat(ArenaFormat.render(mapOf(frozenPass())).elevations()).isNull();
    }

    @Test
    void theWrongNumberOfRowsIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(
                new ArenaDocument(null, null, 2, LEGEND, List.of(". . .", ". . . ."), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("needs 5 grid rows");
    }

    @Test
    void aRowOfTheWrongWidthNamesTheRowAndTheCounts() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(new ArenaDocument(null, null, 2, LEGEND,
                List.of("~ . .", ". f . ^ .", ". . . f .", ". ^ . .", ". . ~"), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("grid row 2 (r=-1) has 5 cells, expected 4");
    }

    @Test
    void aSymbolMissingFromTheLegendIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(new ArenaDocument(null, null, 2, LEGEND,
                List.of("~ . x", ". f . ^", ". . . f .", ". ^ . .", ". . ~"), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not define");
    }

    @Test
    void aMultiCharacterLegendKeyIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(
                new ArenaDocument(null, null, 0, Map.of("..", "PLAIN"), List.of("."), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("single character");
    }

    @Test
    void anUnknownTerrainNameIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(
                new ArenaDocument(null, null, 0, Map.of(".", "SWAMP"), List.of("."), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unknown terrain");
    }

    @Test
    void anElevationRowOfTheWrongWidthIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(new ArenaDocument(null, null, 2, LEGEND, GRID,
                List.of(List.of(0, 1), List.of(1, 2, 3, 4), List.of(2, 3, 9, 3, 2),
                        List.of(1, 2, 3, 4), List.of(0, 1, 2)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("elevation row 1 (r=-2) has 2 cells, expected 3");
    }

    @Test
    void anElevationOutOfRangeIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(new ArenaDocument(null, null, 0,
                Map.of(".", "PLAIN"), List.of("."), List.of(List.of(500)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("outside 0..100");
    }

    @Test
    void aNegativeRadiusIsRejected() {
        assertThatThrownBy(() -> ArenaFormat.tilesOf(
                new ArenaDocument(null, null, -1, LEGEND, GRID, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be negative");
    }

    @Test
    void rowWidthsAddUpToTheHexagonalNumber() {
        for (int radius = 0; radius <= 6; radius++) {
            int cells = 0;
            for (int r = -radius; r <= radius; r++) {
                cells += ArenaFormat.rowOf(radius, r).size();
            }
            assertThat(cells).isEqualTo(MapGenerator.tileCountFor(radius));
        }
    }

    @Test
    void aSingleTileArenaIsValid() {
        List<HexTile> tiles = ArenaFormat.tilesOf(
                new ArenaDocument("Dot", null, 0, Map.of(".", "PLAIN"), List.of("."), null));

        assertThat(tiles).singleElement()
                .satisfies(tile -> assertThat(tile.coordinate()).isEqualTo(HexCoordinate.origin()));
    }
}
