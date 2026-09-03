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
import org.ttarena.arena_map.model.TerrainType;
import org.ttarena.arena_map.repository.GameMapRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class GameMapService {

    private static final int MAX_DEPLOYMENTS = 8;

    private final GameMapRepository repository;
    private final MapProperties properties;
    private final Clock clock;

    public GameMapService(GameMapRepository repository,
                          MapProperties properties,
                          Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    public Flux<GameMap> mapsOwnedBy(String ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    public Flux<GameMap> searchByName(String name) {
        return name == null || name.isBlank()
                ? repository.findAll()
                : repository.findByNameContainingIgnoreCase(name);
    }

    public Mono<GameMap> getById(String id) {
        return repository.findById(id).switchIfEmpty(Mono.error(mapNotFound(id)));
    }

    public Mono<GameMap> create(CreateMapRequest request, String ownerId) {
        return withinOwnerQuota(ownerId).then(Mono.defer(() -> {
            GameMap map = newMap(request.name(), request.description(), ownerId);
            return repository.save(map);
        }));
    }

    public Mono<GameMap> generate(GenerateMapRequest request, String ownerId) {
        if (request.radius() > properties.maxRadius()) {
            return Mono.error(new BadRequestException(
                    "radius %d exceeds the maximum of %d.".formatted(request.radius(), properties.maxRadius())));
        }

        return withinOwnerQuota(ownerId).then(Mono.defer(() -> {
            GameMap map = newMap(request.name(), request.description(), ownerId);
            TerrainType terrain = request.terrain() == null ? TerrainType.PLAIN : request.terrain();
            MapGenerator.fill(map, request.radius(), TileFactory.uniform(terrain));
            return repository.save(map);
        }));
    }

    /**
     * Creates a map from a hand-authored arena document. This is how maps are
     * made: you draw the grid, the service stores exactly what you drew.
     */
    public Mono<GameMap> importArena(ArenaDocument arena, String ownerId) {
        return withinOwnerQuota(ownerId).then(Mono.defer(() -> {
            if (arena.radius() > properties.maxRadius()) {
                return Mono.error(new BadRequestException(
                        "radius %d exceeds the maximum of %d.".formatted(arena.radius(), properties.maxRadius())));
            }

            List<HexTile> tiles = ArenaFormat.tilesOf(arena);
            GameMap map = newMap(arena.name(), arena.description(), ownerId);
            map.setRadius(arena.radius());
            tiles.forEach(map::putTile);
            return repository.save(map);
        }));
    }

    /** Redraws an existing map from an arena document, keeping its id and owner. */
    public Mono<GameMap> replaceGrid(String id, String ownerId, ArenaDocument arena) {
        return ownedMap(id, ownerId).flatMap(map -> {
            if (arena.radius() > properties.maxRadius()) {
                return Mono.error(new BadRequestException(
                        "radius %d exceeds the maximum of %d.".formatted(arena.radius(), properties.maxRadius())));
            }

            List<HexTile> tiles = ArenaFormat.tilesOf(arena);
            if (arena.name() != null && !arena.name().isBlank()) {
                map.setName(arena.name());
            }
            if (arena.description() != null && !arena.description().isBlank()) {
                map.setDescription(arena.description());
            }
            map.setRadius(arena.radius());
            map.getTiles().clear();
            tiles.forEach(map::putTile);
            return save(map);
        });
    }

    /** The same document you would author, so a map round-trips through an editor. */
    public Mono<ArenaDocument> exportGrid(String id) {
        return getById(id).map(ArenaFormat::render);
    }

    public Mono<GameMap> update(String id, String ownerId, UpdateMapRequest request) {
        return ownedMap(id, ownerId).flatMap(map -> {
            if (request.name() != null) {
                map.setName(request.name());
            }
            if (request.description() != null) {
                map.setDescription(request.description());
            }
            return save(map);
        });
    }

    public Mono<Void> delete(String id, String ownerId) {
        return ownedMap(id, ownerId).flatMap(repository::delete);
    }

    public Mono<HexTile> tileAt(String id, HexCoordinate coordinate) {
        return getById(id).flatMap(map -> Mono.justOrEmpty(map.tileAt(coordinate))
                .switchIfEmpty(Mono.error(tileNotFound(id, coordinate))));
    }

    public Mono<GameMap> placeTile(String id, String ownerId, HexCoordinate coordinate, PlaceTileRequest request) {
        return ownedMap(id, ownerId).flatMap(map -> {
            map.putTile(new HexTile(coordinate, request.terrain(), request.elevation()));
            return save(map);
        });
    }

    public Mono<GameMap> removeTile(String id, String ownerId, HexCoordinate coordinate) {
        return ownedMap(id, ownerId).flatMap(map -> map.removeTile(coordinate)
                ? save(map)
                : Mono.error(tileNotFound(id, coordinate)));
    }

    public Mono<List<HexCoordinate>> deployments(String id, int count) {
        if (count < 1 || count > MAX_DEPLOYMENTS) {
            return Mono.error(new BadRequestException(
                    "count must be between 1 and %d.".formatted(MAX_DEPLOYMENTS)));
        }
        return getById(id).flatMap(map -> Mono.fromCallable(() -> {
            List<HexCoordinate> spots = DeploymentPlanner.plan(map, count);
            if (spots.size() < count) {
                throw new BadRequestException(
                        "Map %s has only %d passable tiles, %d were asked for.".formatted(id, spots.size(), count));
            }
            return spots;
        }).subscribeOn(Schedulers.parallel()));
    }

    public Mono<PathResponse> findPath(String id, HexCoordinate from, HexCoordinate to) {
        return getById(id).flatMap(map -> Mono.fromCallable(() -> {
            List<HexCoordinate> path = HexPathfinder.shortestPath(map, from, to);
            return PathResponse.of(path, HexPathfinder.pathCost(map, path));
        }).subscribeOn(Schedulers.parallel()));
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

    private Mono<GameMap> save(GameMap map) {
        map.setUpdatedAt(clock.instant());
        return repository.save(map);
    }

    private Mono<GameMap> ownedMap(String id, String ownerId) {
        return getById(id).flatMap(map -> map.getOwnerId() != null && map.getOwnerId().equals(ownerId)
                ? Mono.just(map)
                : Mono.error(new ForbiddenException("Map %s belongs to another account.".formatted(id))));
    }

    private Mono<Void> withinOwnerQuota(String ownerId) {
        return repository.countByOwnerId(ownerId)
                .filter(owned -> owned >= properties.maxPerOwner())
                .flatMap(owned -> Mono.<Void>error(new BadRequestException(
                        "You already own %d maps, the maximum is %d.".formatted(owned, properties.maxPerOwner()))));
    }

    private static NotFoundException mapNotFound(String id) {
        return new NotFoundException("No map with id " + id + ".");
    }

    private static NotFoundException tileNotFound(String id, HexCoordinate coordinate) {
        return new NotFoundException("Map %s has no tile at %s.".formatted(id, coordinate.key()));
    }
}
