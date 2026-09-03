package org.ttarena.arena_map.repository;

import java.time.Instant;

/**
 * A closed projection, so listing maps reads only these fields out of MongoDB.
 * A radius 32 arena holds over three thousand tiles and no listing needs them.
 */
public interface MapSummary {

    String getId();

    String getOwnerId();

    String getName();

    String getDescription();

    int getRadius();

    int getTileCount();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
