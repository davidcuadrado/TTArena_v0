package org.ttarena.arena_map.model;

public enum TerrainType {

    PLAIN(true, 1),
    FOREST(true, 2),
    HILLS(true, 2),
    DESERT(true, 3),
    WATER(false, 0),
    MOUNTAIN(false, 0);

    private final boolean passable;
    private final int movementCost;

    TerrainType(boolean passable, int movementCost) {
        this.passable = passable;
        this.movementCost = movementCost;
    }

    public boolean passable() {
        return passable;
    }

    public int movementCost() {
        return movementCost;
    }
}
