package org.ttarena.arena_map.service;

import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;

/**
 * Fills a blank arena. Maps are authored by hand, so the only filling on offer
 * is a flat canvas to start drawing on.
 */
@FunctionalInterface
public interface TileFactory {

    HexTile create(HexCoordinate coordinate);

    static TileFactory uniform(TerrainType terrain) {
        return coordinate -> new HexTile(coordinate, terrain, 0);
    }
}
