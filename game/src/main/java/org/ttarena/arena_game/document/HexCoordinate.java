package org.ttarena.arena_game.document;

import org.ttarena.arena_game.exception.BadRequestException;

public record HexCoordinate(int q, int r, int s) {

    private static final String KEY_SEPARATOR = ":";

    public HexCoordinate {
        if (q + r + s != 0) {
            throw new BadRequestException(
                    "Cube coordinates must satisfy q + r + s = 0, got q=%d, r=%d, s=%d.".formatted(q, r, s));
        }
    }

    public int distanceTo(HexCoordinate other) {
        return (Math.abs(q - other.q) + Math.abs(r - other.r) + Math.abs(s - other.s)) / 2;
    }

    public String key() {
        return q + KEY_SEPARATOR + r + KEY_SEPARATOR + s;
    }
}
