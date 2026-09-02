package org.ttarena.arena_map.model;

public final class MapName {

    public static final String PATTERN = "^[\\p{L}\\p{N}][\\p{L}\\p{N} '\\-]*$";
    public static final String MESSAGE =
            "map name must start with a letter or digit and may contain letters, digits, spaces, apostrophes and hyphens";
    public static final int MAX_LENGTH = 64;

    private MapName() {
    }
}
