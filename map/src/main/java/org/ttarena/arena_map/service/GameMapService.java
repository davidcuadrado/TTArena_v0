package org.ttarena.arena_map.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.document.HexCoordinate;
import org.ttarena.arena_map.document.HexTile;
import org.ttarena.arena_map.exception.HexTileNotFoundException;
import org.ttarena.arena_map.exception.InvalidHexCoordinateException;
import org.ttarena.arena_map.exception.MapNotFoundException;
import org.ttarena.arena_map.repository.GameMapRepository;
import org.ttarena.arena_map.util.HexUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class GameMapService {

    private final GameMapRepository mapRepository;

    @Autowired
    public GameMapService(GameMapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public Flux<GameMap> getAllMaps() {
        return mapRepository.findAll();
    }

    public Flux<GameMap> findMapsByName(String name) {
        return mapRepository.findByNameContainingIgnoreCase(name);
    }

    public Flux<GameMap> findMapsByAuthor(String author) {
        return mapRepository.findByAuthor(author);
    }

    public Mono<GameMap> getMapById(String id) {
        return mapRepository.findById(id)
                .switchIfEmpty(Mono.error(new MapNotFoundException(id)));
    }

    public Mono<GameMap> createMap(GameMap map) {

        LocalDateTime now = LocalDateTime.now();
        map.setCreatedAt(now);
        map.setUpdatedAt(now);
        
        return mapRepository.save(map);
    }

    public Mono<GameMap> updateMap(String id, GameMap updatedMap) {
        return mapRepository.findById(id)
                .switchIfEmpty(Mono.error(new MapNotFoundException(id)))
                .flatMap(existingMap -> {
                    // Actualizar propiedades básicas
                    existingMap.setName(updatedMap.getName());
                    existingMap.setDescription(updatedMap.getDescription());
                    existingMap.setWidth(updatedMap.getWidth());
                    existingMap.setHeight(updatedMap.getHeight());
                    existingMap.setUpdatedAt(LocalDateTime.now());
                    
                    // Mantener los metadatos y hexágonos existentes si no se proporcionan nuevos
                    if (updatedMap.getMetadata() != null) {
                        existingMap.setMetadata(updatedMap.getMetadata());
                    }
                    
                    // Solo actualizar los hexágonos si se proporcionan nuevos
                    if (updatedMap.getTiles() != null && !updatedMap.getTiles().isEmpty()) {
                        existingMap.setTiles(updatedMap.getTiles());
                    }
                    
                    return mapRepository.save(existingMap);
                });
    }

    public Mono<Void> deleteMap(String id) {
        return mapRepository.findById(id)
                .switchIfEmpty(Mono.error(new MapNotFoundException(id)))
                .flatMap(mapRepository::delete);
    }

    public Mono<GameMap> addTileToMap(String mapId, HexTile tile) {

        HexCoordinate coord = tile.getCoordinate();
        if (coord == null || !coord.isValid()) {
            return Mono.error(new InvalidHexCoordinateException(
                    coord != null ? coord.getQ() : 0,
                    coord != null ? coord.getR() : 0,
                    coord != null ? coord.getS() : 0));
        }
        
        return mapRepository.findById(mapId)
                .switchIfEmpty(Mono.error(new MapNotFoundException(mapId)))
                .flatMap(map -> {
                    map.addTile(tile);
                    map.setUpdatedAt(LocalDateTime.now());
                    return mapRepository.save(map);
                });
    }

    public Mono<HexTile> getTileFromMap(String mapId, int q, int r, int s) {

        if (q + r + s != 0) {
            return Mono.error(new InvalidHexCoordinateException(q, r, s));
        }
        
        HexCoordinate coordinate = new HexCoordinate(q, r, s);
        String coordKey = HexUtils.getHexKey(q, r, s);
        
        return mapRepository.findById(mapId)
                .switchIfEmpty(Mono.error(new MapNotFoundException(mapId)))
                .flatMap(map -> {
                    HexTile tile = map.getTile(coordinate);
                    if (tile == null) {
                        return Mono.error(new HexTileNotFoundException(mapId, coordKey));
                    }
                    return Mono.just(tile);
                });
    }

    public Mono<GameMap> updateTileInMap(String mapId, int q, int r, int s, HexTile updatedTile) {

        if (q + r + s != 0) {
            return Mono.error(new InvalidHexCoordinateException(q, r, s));
        }

        updatedTile.setCoordinate(new HexCoordinate(q, r, s));
        
        String coordKey = HexUtils.getHexKey(q, r, s);
        
        return mapRepository.findById(mapId)
                .switchIfEmpty(Mono.error(new MapNotFoundException(mapId)))
                .flatMap(map -> {
                    if (!map.hasTile(new HexCoordinate(q, r, s))) {
                        return Mono.error(new HexTileNotFoundException(mapId, coordKey));
                    }
                    
                    map.addTile(updatedTile); // Sobrescribe el hexágono existente
                    map.setUpdatedAt(LocalDateTime.now());
                    return mapRepository.save(map);
                });
    }

    public Mono<GameMap> removeTileFromMap(String mapId, int q, int r, int s) {
        if (q + r + s != 0) {
            return Mono.error(new InvalidHexCoordinateException(q, r, s));
        }
        
        HexCoordinate coordinate = new HexCoordinate(q, r, s);
        String coordKey = HexUtils.getHexKey(q, r, s);
        
        return mapRepository.findById(mapId)
                .switchIfEmpty(Mono.error(new MapNotFoundException(mapId)))
                .flatMap(map -> {
                    if (!map.removeTile(coordinate)) {
                        return Mono.error(new HexTileNotFoundException(mapId, coordKey));
                    }
                    
                    map.setUpdatedAt(LocalDateTime.now());
                    return mapRepository.save(map);
                });
    }

    public Mono<GameMap> generateEmptyHexagonalMap(String name, String description, String author, int radius, String defaultTerrainType) {
        GameMap map = GameMap.builder()
                .name(name)
                .description(description)
                .author(author)
                .width(radius * 2 + 1)
                .height(radius * 2 + 1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        

        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            
            for (int r = r1; r <= r2; r++) {
                int s = -q - r;
                HexCoordinate coord = new HexCoordinate(q, r, s);
                
                HexTile tile = HexTile.builder()
                        .coordinate(coord)
                        .terrainType(defaultTerrainType)
                        .elevation(0)
                        .passable(true)
                        .movementCost(1)
                        .build();
                
                map.addTile(tile);
            }
        }
        
        return mapRepository.save(map);
    }
}
