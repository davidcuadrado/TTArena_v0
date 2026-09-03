package org.ttarena.arena_map.model;

import org.ttarena.arena_map.exception.BadRequestException;

import java.util.Arrays;

public enum TerrainType {

    PLAIN('.', true, 1),
    FOREST('f', true, 2),
    HILLS('h', true, 2),
    DESERT('d', true, 3),
    WATER('~', false, 0),
    MOUNTAIN('^', false, 0);

    private final char symbol;
    private final boolean passable;
    private final int movementCost;

    TerrainType(char symbol, boolean passable, int movementCost) {
        this.symbol = symbol;
        this.passable = passable;
        this.movementCost = movementCost;
    }

    public char symbol() {
        return symbol;
    }

    public boolean passable() {
        return passable;
    }

    public int movementCost() {
        return movementCost;
    }

    public static TerrainType named(String name) {
        return Arrays.stream(values())
                .filter(terrain -> terrain.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Unknown terrain '%s'. Known terrain: %s.".formatted(name, Arrays.toString(values()))));
    }
}
