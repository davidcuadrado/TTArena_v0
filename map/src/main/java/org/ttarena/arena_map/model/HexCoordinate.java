package org.ttarena.arena_map.model;

import org.ttarena.arena_map.exception.InvalidHexCoordinateException;

import java.util.Arrays;
import java.util.List;

public record HexCoordinate(int q, int r, int s) {

    private static final String KEY_SEPARATOR = ":";

    public HexCoordinate {
        if (q + r + s != 0) {
            throw new InvalidHexCoordinateException(q, r, s);
        }
    }

    public static HexCoordinate axial(int q, int r) {
        return new HexCoordinate(q, r, -q - r);
    }

    public static HexCoordinate origin() {
        return new HexCoordinate(0, 0, 0);
    }

    public static HexCoordinate parse(String key) {
        String[] parts = key == null ? new String[0] : key.split(KEY_SEPARATOR);
        if (parts.length != 3) {
            throw new InvalidHexCoordinateException(key);
        }
        try {
            return new HexCoordinate(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException e) {
            throw new InvalidHexCoordinateException(key);
        }
    }

    public String key() {
        return q + KEY_SEPARATOR + r + KEY_SEPARATOR + s;
    }

    public int distanceTo(HexCoordinate other) {
        return (Math.abs(q - other.q) + Math.abs(r - other.r) + Math.abs(s - other.s)) / 2;
    }

    public List<HexCoordinate> neighbours() {
        return Arrays.stream(HexDirection.values()).map(direction -> direction.from(this)).toList();
    }

    public int ringIndex() {
        return Math.max(Math.abs(q), Math.max(Math.abs(r), Math.abs(s)));
    }
}
