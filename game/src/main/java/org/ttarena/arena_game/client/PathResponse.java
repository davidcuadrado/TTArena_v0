package org.ttarena.arena_game.client;

import org.ttarena.arena_game.document.HexCoordinate;

import java.util.List;

public record PathResponse(List<HexCoordinate> path, int movementCost, boolean reachable) {
}
