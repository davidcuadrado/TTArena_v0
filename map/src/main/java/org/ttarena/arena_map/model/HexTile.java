package org.ttarena.arena_map.model;

public record HexTile(HexCoordinate coordinate, TerrainType terrain, int elevation) {

    public static HexTile of(HexCoordinate coordinate, TerrainType terrain) {
        return new HexTile(coordinate, terrain, 0);
    }

    public boolean passable() {
        return terrain.passable();
    }

    public int movementCost() {
        return terrain.movementCost();
    }
}
