package org.ttarena.arena_map.service;

import org.springframework.stereotype.Service;
import org.ttarena.arena_map.config.MapProperties;
import org.ttarena.arena_map.document.GameMap;
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

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.random.RandomGenerator;

@Service
public class GameMapService {

    private final GameMapRepository repository;
    private final MapProperties properties;
    private final Clock clock;
    private final RandomGenerator random;

    public GameMapService(GameMapRepository repository,
                          MapProperties properties,
                          Clock clock,
                          RandomGenerator random) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
        this.random = random;
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
            TerrainType terrain = request.terrain();
            MapGenerator.fill(map, request.radius(),
                    terrain == null ? TileFactory.random(random) : TileFactory.uniform(terrain));
            return repository.save(map);
        }));
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

    public Mono<PathResponse> findPath(String id, HexCoordinate from, HexCoordinate to) {
        return getById(id).map(map -> {
            List<HexCoordinate> path = HexPathfinder.shortestPath(map, from, to);
            return PathResponse.of(path, HexPathfinder.pathCost(map, path));
        });
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
