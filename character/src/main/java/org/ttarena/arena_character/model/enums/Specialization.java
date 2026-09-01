package org.ttarena.arena_character.model.enums;

import java.util.Map;

/**
 * Common contract for the per-class specialization enums.
 *
 * <p>Each specialization owns two pieces of data: the {@link Role} it fills in a
 * group, and the base stats a character of that specialization starts with.
 * Keeping the stats here - rather than in a {@code switch} inside every
 * character constructor - means a class's numbers live in exactly one place,
 * next to the role they belong with.
 */
public interface Specialization {

    /** The enum constant name, e.g. {@code "ARMS"}. Provided by {@link Enum}. */
    String name();

    Role getRole();

    /**
     * Base stat values for this specialization. Stats a class does not use are
     * simply absent from the map, so callers should use
     * {@code getOrDefault(stat, 0)}.
     */
    Map<StatType, Integer> getBaseStats();
}
