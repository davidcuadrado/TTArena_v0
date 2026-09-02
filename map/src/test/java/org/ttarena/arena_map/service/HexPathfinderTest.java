package org.ttarena.arena_map.service;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HexPathfinderTest {

    private static GameMap plainMap(int radius) {
        GameMap map = new GameMap();
        MapGenerator.fill(map, radius, TileFactory.uniform(TerrainType.PLAIN));
        return map;
    }

    private static void putTerrain(GameMap map, HexCoordinate coordinate, TerrainType terrain) {
        map.putTile(new HexTile(coordinate, terrain, 0));
    }

    @Test
    void aPathToItselfIsASingleTile() {
        GameMap map = plainMap(2);
        assertThat(HexPathfinder.shortestPath(map, HexCoordinate.origin(), HexCoordinate.origin()))
                .containsExactly(HexCoordinate.origin());
    }

    @Test
    void takesTheStraightLineAcrossOpenGround() {
        GameMap map = plainMap(4);
        HexCoordinate goal = HexCoordinate.axial(3, 0);

        List<HexCoordinate> path = HexPathfinder.shortestPath(map, HexCoordinate.origin(), goal);

        assertThat(path).startsWith(HexCoordinate.origin()).endsWith(goal);
        assertThat(path).hasSize(HexCoordinate.origin().distanceTo(goal) + 1);
    }

    @Test
    void walksAroundImpassableTerrain() {
        GameMap map = plainMap(4);
        HexCoordinate goal = HexCoordinate.axial(2, 0);
        HexCoordinate.origin().neighbours().stream()
                .filter(neighbour -> neighbour.distanceTo(goal) < HexCoordinate.origin().distanceTo(goal))
                .forEach(neighbour -> putTerrain(map, neighbour, TerrainType.MOUNTAIN));

        List<HexCoordinate> path = HexPathfinder.shortestPath(map, HexCoordinate.origin(), goal);

        assertThat(path).isNotEmpty().endsWith(goal);
        assertThat(path).noneSatisfy(step ->
                assertThat(map.tileAt(step).terrain()).isEqualTo(TerrainType.MOUNTAIN));
        assertThat(path).hasSizeGreaterThan(HexCoordinate.origin().distanceTo(goal) + 1);
    }

    @Test
    void prefersTheCheaperRouteOverTheShorterOne() {
        GameMap map = plainMap(3);
        HexCoordinate goal = HexCoordinate.axial(2, 0);
        putTerrain(map, HexCoordinate.axial(1, 0), TerrainType.DESERT);

        List<HexCoordinate> path = HexPathfinder.shortestPath(map, HexCoordinate.origin(), goal);

        assertThat(path).doesNotContain(HexCoordinate.axial(1, 0));
        assertThat(HexPathfinder.pathCost(map, path)).isEqualTo(3);
    }

    @Test
    void reportsNoPathWhenTheGoalIsWalledIn() {
        GameMap map = plainMap(4);
        HexCoordinate goal = HexCoordinate.axial(2, 0);
        goal.neighbours().forEach(neighbour -> putTerrain(map, neighbour, TerrainType.WATER));

        assertThat(HexPathfinder.shortestPath(map, HexCoordinate.origin(), goal)).isEmpty();
    }

    @Test
    void reportsNoPathWhenAnEndpointIsMissingFromTheMap() {
        GameMap map = plainMap(1);
        assertThat(HexPathfinder.shortestPath(map, HexCoordinate.origin(), HexCoordinate.axial(9, 0))).isEmpty();
    }

    @Test
    void pathCostChargesForEveryTileEntered() {
        GameMap map = plainMap(3);
        List<HexCoordinate> path = HexPathfinder.shortestPath(map, HexCoordinate.origin(), HexCoordinate.axial(3, 0));

        assertThat(HexPathfinder.pathCost(map, path)).isEqualTo(3);
    }
}
