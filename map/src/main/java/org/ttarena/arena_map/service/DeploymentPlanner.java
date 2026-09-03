package org.ttarena.arena_map.service;

import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Picks starting positions that are passable and as far apart as the arena
 * allows, so neither player begins the game already on top of the other.
 */
public final class DeploymentPlanner {

    private DeploymentPlanner() {
    }

    public static List<HexCoordinate> plan(GameMap map, int count) {
        List<HexCoordinate> candidates = map.allTiles().stream()
                .filter(HexTile::passable)
                .map(HexTile::coordinate)
                .toList();

        if (candidates.isEmpty() || count <= 0) {
            return List.of();
        }

        List<HexCoordinate> chosen = new ArrayList<>(count);
        chosen.add(candidates.stream()
                .max(Comparator.comparingInt(HexCoordinate::ringIndex))
                .orElseThrow());

        while (chosen.size() < count && chosen.size() < candidates.size()) {
            chosen.add(candidates.stream()
                    .filter(candidate -> !chosen.contains(candidate))
                    .max(Comparator.comparingInt(candidate -> nearestChosenDistance(chosen, candidate)))
                    .orElseThrow());
        }

        return List.copyOf(chosen);
    }

    private static int nearestChosenDistance(List<HexCoordinate> chosen, HexCoordinate candidate) {
        return chosen.stream().mapToInt(candidate::distanceTo).min().orElse(0);
    }
}
