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

/**
 * Servicio que implementa la lógica de negocio para la gestión de mapas hexagonales.
 * Proporciona métodos para crear, consultar, actualizar y eliminar mapas y hexágonos.
 */
@Service
public class GameMapService {

    private final GameMapRepository mapRepository;

    @Autowired
    public GameMapService(GameMapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    /**
     * Obtiene todos los mapas disponibles.
     *
     * @return Flujo de todos los mapas
     */
    public Flux<GameMap> getAllMaps() {
        return mapRepository.findAll();
    }

    /**
     * Busca mapas por nombre (búsqueda parcial).
     *
     * @param name Nombre o parte del nombre a buscar
     * @return Flujo de mapas que coinciden con el criterio de búsqueda
     */
    public Flux<GameMap> findMapsByName(String name) {
        return mapRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Busca mapas por autor.
     *
     * @param author Nombre del autor
     * @return Flujo de mapas creados por el autor especificado
     */
    public Flux<GameMap> findMapsByAuthor(String author) {
        return mapRepository.findByAuthor(author);
    }

    /**
     * Obtiene un mapa por su ID.
     *
     * @param id ID del mapa
     * @return Mono con el mapa encontrado
     * @throws MapNotFoundException si no se encuentra el mapa
     */
    public Mono<GameMap> getMapById(String id) {
        return mapRepository.findById(id)
                .switchIfEmpty(Mono.error(new MapNotFoundException(id)));
    }

    /**
     * Crea un nuevo mapa.
     *
     * @param map Mapa a crear
     * @return Mono con el mapa creado
     */
    public Mono<GameMap> createMap(GameMap map) {
        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        map.setCreatedAt(now);
        map.setUpdatedAt(now);
        
        return mapRepository.save(map);
    }

    /**
     * Actualiza un mapa existente.
     *
     * @param id ID del mapa a actualizar
     * @param updatedMap Datos actualizados del mapa
     * @return Mono con el mapa actualizado
     * @throws MapNotFoundException si no se encuentra el mapa
     */
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

    /**
     * Elimina un mapa por su ID.
     *
     * @param id ID del mapa a eliminar
     * @return Mono vacío que completa cuando se elimina el mapa
     * @throws MapNotFoundException si no se encuentra el mapa
     */
    public Mono<Void> deleteMap(String id) {
        return mapRepository.findById(id)
                .switchIfEmpty(Mono.error(new MapNotFoundException(id)))
                .flatMap(mapRepository::delete);
    }

    /**
     * Añade un hexágono a un mapa existente.
     *
     * @param mapId ID del mapa
     * @param tile Hexágono a añadir
     * @return Mono con el mapa actualizado
     * @throws MapNotFoundException si no se encuentra el mapa
     * @throws InvalidHexCoordinateException si las coordenadas del hexágono son inválidas
     */
    public Mono<GameMap> addTileToMap(String mapId, HexTile tile) {
        // Validar coordenadas del hexágono
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

    /**
     * Obtiene un hexágono de un mapa por sus coordenadas.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @return Mono con el hexágono encontrado
     * @throws MapNotFoundException si no se encuentra el mapa
     * @throws InvalidHexCoordinateException si las coordenadas son inválidas
     * @throws HexTileNotFoundException si no se encuentra el hexágono
     */
    public Mono<HexTile> getTileFromMap(String mapId, int q, int r, int s) {
        // Validar coordenadas
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

    /**
     * Actualiza un hexágono en un mapa.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @param updatedTile Datos actualizados del hexágono
     * @return Mono con el mapa actualizado
     * @throws MapNotFoundException si no se encuentra el mapa
     * @throws InvalidHexCoordinateException si las coordenadas son inválidas
     * @throws HexTileNotFoundException si no se encuentra el hexágono
     */
    public Mono<GameMap> updateTileInMap(String mapId, int q, int r, int s, HexTile updatedTile) {
        // Validar coordenadas
        if (q + r + s != 0) {
            return Mono.error(new InvalidHexCoordinateException(q, r, s));
        }
        
        // Asegurar que las coordenadas del hexágono actualizado coincidan con las proporcionadas
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

    /**
     * Elimina un hexágono de un mapa.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @return Mono con el mapa actualizado
     * @throws MapNotFoundException si no se encuentra el mapa
     * @throws InvalidHexCoordinateException si las coordenadas son inválidas
     * @throws HexTileNotFoundException si no se encuentra el hexágono
     */
    public Mono<GameMap> removeTileFromMap(String mapId, int q, int r, int s) {
        // Validar coordenadas
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

    /**
     * Genera un mapa hexagonal vacío con dimensiones específicas.
     *
     * @param name Nombre del mapa
     * @param description Descripción del mapa
     * @param author Autor del mapa
     * @param radius Radio del mapa (número de anillos de hexágonos desde el centro)
     * @param defaultTerrainType Tipo de terreno predeterminado para los hexágonos
     * @return Mono con el mapa generado
     */
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
        
        // Generar hexágonos en un patrón circular con el radio especificado
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
