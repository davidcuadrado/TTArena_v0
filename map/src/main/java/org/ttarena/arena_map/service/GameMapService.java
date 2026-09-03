package org.ttarena.arena_map.service;

import org.springframework.stereotype.Service;
import org.ttarena.arena_map.config.MapProperties;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.dto.ArenaDocument;
import org.ttarena.arena_map.dto.CreateMapRequest;
import org.ttarena.arena_map.dto.GenerateMapRequest;
import org.ttarena.arena_map.dto.PathResponse;
import org.ttarena.arena_map.dto.PlaceTileRequest;
import org.ttarena.arena_map.dto.UpdateMapRequest;
import org.ttarena.arena_map.exception.BadRequestException;
import org.ttarena.arena_map.exception.ForbiddenException;
import org.ttarena.arena_map.exception.NotFoundException;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.repository.GameMapRepository;
import org.ttarena.arena_map.repository.MapSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Every operation that walks a whole arena - parsing it, rendering it, filling
 * it, pathfinding across it - runs on the parallel scheduler. A radius 32 map
 * is over three thousand tiles, which is real work and does not belong on an
 * event loop thread that other requests are waiting on.
 */
@Service
public class GameMapService {

    private static final int MAX_STARTING_POSITIONS = 8;

    private final GameMapRepository maps;
    private final MapProperties mapProperties;
    private final Clock clock;

    public GameMapService(GameMapRepository maps, MapProperties mapProperties, Clock clock) {
        this.maps = maps;
        this.mapProperties = mapProperties;
        this.clock = clock;
    }

    public Flux<MapSummary> mapsOwnedBy(String ownerId) {
        return maps.findSummaryByOwnerId(ownerId);
    }

    public Flux<MapSummary> searchByName(String name) {
        return name == null || name.isBlank()
                ? maps.findSummaryBy()
                : maps.findSummaryByNameContainingIgnoreCase(name);
    }

    public Mono<GameMap> getById(String mapId) {
        return maps.findById(mapId).switchIfEmpty(Mono.defer(() -> Mono.error(mapNotFound(mapId))));
    }

    public Mono<GameMap> create(CreateMapRequest request, String ownerId) {
        return refuseWhenLargerThanAllowed(request.radius())
                .then(refuseWhenQuotaReached(ownerId))
                .then(Mono.defer(() -> {
                    GameMap map = newMap(request.name(), request.description(), ownerId);
                    map.setRadius(request.radius());
                    return maps.save(map);
                }));
    }

    /** Lays down a flat canvas of one terrain to author on top of. */
    public Mono<GameMap> generate(GenerateMapRequest request, String ownerId) {
        if (request.terrain() == null) {
            return Mono.error(new BadRequestException("A generated canvas needs a terrain to fill it with."));
        }

        return refuseWhenLargerThanAllowed(request.radius())
                .then(refuseWhenQuotaReached(ownerId))
                .then(Mono.fromCallable(() -> {
                    GameMap map = newMap(request.name(), request.description(), ownerId);
                    MapGenerator.fill(map, request.radius(), TileFactory.uniform(request.terrain()));
                    return map;
                }).subscribeOn(Schedulers.parallel()))
                .flatMap(maps::save);
    }

    /**
     * Creates a map from a hand-authored arena document. This is how maps are
     * made: you draw the grid, the service stores exactly what you drew.
     */
    public Mono<GameMap> importArena(ArenaDocument arena, String ownerId) {
        return refuseWhenLargerThanAllowed(arena.radius())
                .then(refuseWhenQuotaReached(ownerId))
                .then(tilesOf(arena))
                .map(tiles -> {
                    GameMap map = newMap(arena.name(), arena.description(), ownerId);
                    map.setRadius(arena.radius());
                    tiles.forEach(map::putTile);
                    return map;
                })
                .flatMap(maps::save);
    }

    /** Redraws an existing map from an arena document, keeping its id and owner. */
    public Mono<GameMap> replaceArena(String mapId, String ownerId, ArenaDocument arena) {
        return ownedMap(mapId, ownerId)
                .flatMap(map -> refuseWhenLargerThanAllowed(arena.radius())
                        .then(tilesOf(arena))
                        .flatMap(tiles -> {
                            if (arena.name() != null && !arena.name().isBlank()) {
                                map.setName(arena.name());
                            }
                            if (arena.description() != null && !arena.description().isBlank()) {
                                map.setDescription(arena.description());
                            }
                            map.setRadius(arena.radius());
                            map.clearTiles();
                            tiles.forEach(map::putTile);
                            return touchAndSave(map);
                        }));
    }

    /** The same document you would author, so a map round-trips through an editor. */
    public Mono<ArenaDocument> exportArena(String mapId) {
        return getById(mapId).flatMap(map -> Mono.fromCallable(() -> ArenaFormat.documentOf(map))
                .subscribeOn(Schedulers.parallel()));
    }

    public Mono<GameMap> update(String mapId, String ownerId, UpdateMapRequest request) {
        return ownedMap(mapId, ownerId).flatMap(map -> {
            if (request.name() != null) {
                map.setName(request.name());
            }
            if (request.description() != null) {
                map.setDescription(request.description());
            }
            return touchAndSave(map);
        });
    }

    public Mono<Void> delete(String mapId, String ownerId) {
        return ownedMap(mapId, ownerId).flatMap(maps::delete);
    }

    public Mono<HexTile> tileAt(String mapId, HexCoordinate coordinate) {
        return getById(mapId).flatMap(map -> Mono.justOrEmpty(map.tileAt(coordinate))
                .switchIfEmpty(Mono.defer(() -> Mono.error(tileNotFound(mapId, coordinate)))));
    }

    public Mono<GameMap> placeTile(String mapId, String ownerId, HexCoordinate coordinate, PlaceTileRequest request) {
        return ownedMap(mapId, ownerId).flatMap(map -> {
            if (!map.holds(coordinate)) {
                return Mono.error(new BadRequestException(
                        "%s lies outside a radius %d arena.".formatted(coordinate.key(), map.getRadius())));
            }
            map.putTile(new HexTile(coordinate, request.terrain(), request.elevation()));
            return touchAndSave(map);
        });
    }

    public Mono<GameMap> removeTile(String mapId, String ownerId, HexCoordinate coordinate) {
        return ownedMap(mapId, ownerId).flatMap(map -> map.removeTile(coordinate)
                ? touchAndSave(map)
                : Mono.error(tileNotFound(mapId, coordinate)));
    }

    public Mono<List<HexCoordinate>> startingPositions(String mapId, int howMany) {
        if (howMany < 1 || howMany > MAX_STARTING_POSITIONS) {
            return Mono.error(new BadRequestException(
                    "count must be between 1 and %d.".formatted(MAX_STARTING_POSITIONS)));
        }

        return getById(mapId).flatMap(map -> Mono.fromCallable(() -> {
            List<HexCoordinate> positions = DeploymentPlanner.plan(map, howMany);
            if (positions.size() < howMany) {
                throw new BadRequestException("Map %s has only %d passable tiles, %d were asked for."
                        .formatted(mapId, positions.size(), howMany));
            }
            return positions;
        }).subscribeOn(Schedulers.parallel()));
    }

    public Mono<PathResponse> findPath(String mapId, HexCoordinate from, HexCoordinate to) {
        return getById(mapId).flatMap(map -> Mono.fromCallable(() -> {
            List<HexCoordinate> path = HexPathfinder.shortestPath(map, from, to);
            return PathResponse.of(path, HexPathfinder.pathCost(map, path));
        }).subscribeOn(Schedulers.parallel()));
    }

    private Mono<List<HexTile>> tilesOf(ArenaDocument arena) {
        return Mono.fromCallable(() -> ArenaFormat.tilesOf(arena)).subscribeOn(Schedulers.parallel());
    }

    private GameMap newMap(String name, String description, String ownerId) {
        Instant now = clock.instant();
        GameMap map = new GameMap();
        map.setName(name);
        map.setDescription(description);
        map.setOwnerId(ownerId);
        map.setCreatedAt(now);
        map.setUpdatedAt(now);
        return map;
    }

    private Mono<GameMap> touchAndSave(GameMap map) {
        map.setUpdatedAt(clock.instant());
        return maps.save(map);
    }

    private Mono<GameMap> ownedMap(String mapId, String ownerId) {
        return getById(mapId).flatMap(map -> map.getOwnerId() != null && map.getOwnerId().equals(ownerId)
                ? Mono.just(map)
                : Mono.error(new ForbiddenException("Map %s belongs to another account.".formatted(mapId))));
    }

    /**
     * Deferred, so composing this into a chain does not touch the database at
     * assembly time. {@code Mono.then(x)} builds x eagerly even when the
     * upstream is already an error, and building it must therefore be free.
     */
    private Mono<Void> refuseWhenQuotaReached(String ownerId) {
        return Mono.defer(() -> maps.countByOwnerId(ownerId)
                .filter(ownedMaps -> ownedMaps >= mapProperties.maxPerOwner())
                .flatMap(ownedMaps -> Mono.<Void>error(new BadRequestException(
                        "You already own %d maps, the maximum is %d."
                                .formatted(ownedMaps, mapProperties.maxPerOwner())))));
    }

    private Mono<Void> refuseWhenLargerThanAllowed(int radius) {
        return radius > mapProperties.maxRadius()
                ? Mono.error(new BadRequestException(
                        "radius %d exceeds the maximum of %d.".formatted(radius, mapProperties.maxRadius())))
                : Mono.empty();
    }

    private static NotFoundException mapNotFound(String mapId) {
        return new NotFoundException("No map with id " + mapId + ".");
    }

    private static NotFoundException tileNotFound(String mapId, HexCoordinate coordinate) {
        return new NotFoundException("Map %s has no tile at %s.".formatted(mapId, coordinate.key()));
    }
}
