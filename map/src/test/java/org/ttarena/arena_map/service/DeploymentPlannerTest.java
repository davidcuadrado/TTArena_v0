package org.ttarena.arena_map.service;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentPlannerTest {

    private static GameMap arena(int radius, TerrainType terrain) {
        GameMap map = new GameMap();
        MapGenerator.fill(map, radius, TileFactory.uniform(terrain));
        return map;
    }

    @Test
    void putsTwoPlayersAsFarApartAsTheArenaAllows() {
        GameMap map = arena(3, TerrainType.PLAIN);

        List<HexCoordinate> spots = DeploymentPlanner.plan(map, 2);

        assertThat(spots).hasSize(2);
        assertThat(spots.get(0).distanceTo(spots.get(1))).isEqualTo(6);
    }

    @Test
    void neverPlacesAnyoneOnImpassableGround() {
        GameMap map = arena(3, TerrainType.MOUNTAIN);
        HexCoordinate walkable = HexCoordinate.axial(1, 0);
        map.putTile(new HexTile(walkable, TerrainType.PLAIN, 0));

        List<HexCoordinate> spots = DeploymentPlanner.plan(map, 2);

        assertThat(spots).containsExactly(walkable);
    }

    @Test
    void anArenaWithNoPassableGroundOffersNothing() {
        assertThat(DeploymentPlanner.plan(arena(2, TerrainType.WATER), 2)).isEmpty();
    }

    @Test
    void chosenPositionsAreDistinct() {
        List<HexCoordinate> spots = DeploymentPlanner.plan(arena(4, TerrainType.PLAIN), 4);

        assertThat(spots).hasSize(4).doesNotHaveDuplicates();
    }

    @Test
    void everyChosenPositionIsPassable() {
        GameMap map = arena(3, TerrainType.PLAIN);
        map.putTile(new HexTile(HexCoordinate.axial(3, 0), TerrainType.WATER, 0));

        assertThat(DeploymentPlanner.plan(map, 3))
                .allSatisfy(spot -> assertThat(map.tileAt(spot).passable()).isTrue());
    }
}
