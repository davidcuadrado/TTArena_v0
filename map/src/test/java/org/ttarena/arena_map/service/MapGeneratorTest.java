package org.ttarena.arena_map.service;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.TerrainType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapGeneratorTest {

    @Test
    void generatesTheHexagonalNumberOfTiles() {
        for (int radius = 0; radius <= 5; radius++) {
            assertThat(MapGenerator.hexesWithin(radius))
                    .hasSize(MapGenerator.tileCountFor(radius));
        }
    }

    @Test
    void everyGeneratedCoordinateSitsInsideTheRadius() {
        int radius = 4;
        List<HexCoordinate> coordinates = MapGenerator.hexesWithin(radius);

        assertThat(coordinates).doesNotHaveDuplicates();
        assertThat(coordinates).allSatisfy(coordinate ->
                assertThat(coordinate.ringIndex()).isLessThanOrEqualTo(radius));
    }

    @Test
    void fillStampsTheRadiusAndOneTilePerCoordinate() {
        GameMap map = new GameMap();
        MapGenerator.fill(map, 3, TileFactory.uniform(TerrainType.PLAIN));

        assertThat(map.getRadius()).isEqualTo(3);
        assertThat(map.tileCount()).isEqualTo(MapGenerator.tileCountFor(3));
        assertThat(map.allTiles()).allSatisfy(tile ->
                assertThat(tile.terrain()).isEqualTo(TerrainType.PLAIN));
    }
}
