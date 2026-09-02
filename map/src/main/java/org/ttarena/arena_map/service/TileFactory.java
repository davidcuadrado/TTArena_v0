package org.ttarena.arena_map.service;

import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

import java.util.random.RandomGenerator;

@FunctionalInterface
public interface TileFactory {

    HexTile create(HexCoordinate coordinate);

    static TileFactory uniform(TerrainType terrain) {
        return coordinate -> new HexTile(coordinate, terrain, 0);
    }

    static TileFactory random(RandomGenerator random) {
        TerrainType[] terrains = TerrainType.values();
        return coordinate -> {
            TerrainType terrain = terrains[random.nextInt(terrains.length)];
            int elevation = terrain == TerrainType.MOUNTAIN
                    ? 5 + random.nextInt(5)
                    : coordinate.ringIndex() / 2 + random.nextInt(3);
            return new HexTile(coordinate, terrain, elevation);
        };
    }
}
