package org.ttarena.arena_map.dto;

import org.ttarena.arena_map.model.HexCoordinate;

import java.util.List;

public record PathResponse(List<HexCoordinate> path, int movementCost, boolean reachable) {

    public static PathResponse of(List<HexCoordinate> path, int movementCost) {
        return new PathResponse(path, movementCost, !path.isEmpty());
    }
}
