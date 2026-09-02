package org.ttarena.arena_map.exception;

public class InvalidHexCoordinateException extends BadRequestException {

    public InvalidHexCoordinateException(int q, int r, int s) {
        super("Cube coordinates must satisfy q + r + s = 0, got q=%d, r=%d, s=%d.".formatted(q, r, s));
    }

    public InvalidHexCoordinateException(String key) {
        super("Malformed hex key '%s', expected the form q:r:s.".formatted(key));
    }
}
