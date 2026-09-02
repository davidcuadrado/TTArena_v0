package org.ttarena.arena_map.model;

public enum HexDirection {

    NORTH(0, -1, 1),
    NORTH_EAST(1, -1, 0),
    SOUTH_EAST(1, 0, -1),
    SOUTH(0, 1, -1),
    SOUTH_WEST(-1, 1, 0),
    NORTH_WEST(-1, 0, 1);

    private final int deltaQ;
    private final int deltaR;
    private final int deltaS;

    HexDirection(int deltaQ, int deltaR, int deltaS) {
        this.deltaQ = deltaQ;
        this.deltaR = deltaR;
        this.deltaS = deltaS;
    }

    public HexCoordinate from(HexCoordinate origin) {
        return new HexCoordinate(origin.q() + deltaQ, origin.r() + deltaR, origin.s() + deltaS);
    }
}
