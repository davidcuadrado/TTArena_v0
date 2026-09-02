package org.ttarena.arena_map.service;

import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class HexPathfinder {

    private static final int CHEAPEST_POSSIBLE_STEP = 1;

    private HexPathfinder() {
    }

    private record Step(HexCoordinate coordinate, int estimatedTotalCost) {
    }

    public static List<HexCoordinate> shortestPath(GameMap map, HexCoordinate start, HexCoordinate goal) {
        if (!enterable(map, start) || !enterable(map, goal)) {
            return List.of();
        }
        if (start.equals(goal)) {
            return List.of(start);
        }

        Map<HexCoordinate, Integer> costSoFar = new HashMap<>();
        Map<HexCoordinate, HexCoordinate> cameFrom = new HashMap<>();
        Set<HexCoordinate> settled = new HashSet<>();
        PriorityQueue<Step> frontier = new PriorityQueue<>(Comparator.comparingInt(Step::estimatedTotalCost));

        costSoFar.put(start, 0);
        frontier.add(new Step(start, estimate(start, goal)));

        while (!frontier.isEmpty()) {
            HexCoordinate current = frontier.poll().coordinate();
            if (current.equals(goal)) {
                return reconstruct(cameFrom, goal);
            }
            if (!settled.add(current)) {
                continue;
            }

            for (HexCoordinate neighbour : current.neighbours()) {
                if (settled.contains(neighbour) || !enterable(map, neighbour)) {
                    continue;
                }
                int candidateCost = costSoFar.get(current) + map.tileAt(neighbour).movementCost();
                if (candidateCost < costSoFar.getOrDefault(neighbour, Integer.MAX_VALUE)) {
                    costSoFar.put(neighbour, candidateCost);
                    cameFrom.put(neighbour, current);
                    frontier.add(new Step(neighbour, candidateCost + estimate(neighbour, goal)));
                }
            }
        }

        return List.of();
    }

    public static int pathCost(GameMap map, List<HexCoordinate> path) {
        return path.stream().skip(1).mapToInt(coordinate -> map.tileAt(coordinate).movementCost()).sum();
    }

    private static int estimate(HexCoordinate from, HexCoordinate goal) {
        return from.distanceTo(goal) * CHEAPEST_POSSIBLE_STEP;
    }

    private static boolean enterable(GameMap map, HexCoordinate coordinate) {
        HexTile tile = map.tileAt(coordinate);
        return tile != null && tile.passable();
    }

    private static List<HexCoordinate> reconstruct(Map<HexCoordinate, HexCoordinate> cameFrom, HexCoordinate goal) {
        List<HexCoordinate> path = new ArrayList<>();
        for (HexCoordinate step = goal; step != null; step = cameFrom.get(step)) {
            path.add(step);
        }
        return List.copyOf(path.reversed());
    }
}
