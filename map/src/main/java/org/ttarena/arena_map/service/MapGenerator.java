package org.ttarena.arena_map.service;

import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;

import java.util.ArrayList;
import java.util.List;

public final class MapGenerator {

    private MapGenerator() {
    }

    public static List<HexCoordinate> hexesWithin(int radius) {
        List<HexCoordinate> coordinates = new ArrayList<>(tileCountFor(radius));
        for (int q = -radius; q <= radius; q++) {
            int lowestR = Math.max(-radius, -q - radius);
            int highestR = Math.min(radius, -q + radius);
            for (int r = lowestR; r <= highestR; r++) {
                coordinates.add(new HexCoordinate(q, r, -q - r));
            }
        }
        return coordinates;
    }

    public static int tileCountFor(int radius) {
        return 3 * radius * radius + 3 * radius + 1;
    }

    public static void fill(GameMap map, int radius, TileFactory tileFactory) {
        map.setRadius(radius);
        hexesWithin(radius).forEach(coordinate -> map.putTile(tileFactory.create(coordinate)));
    }
}
