package org.ttarena.arena_map.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.document.HexTile;
import org.ttarena.arena_map.service.GameMapService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de mapas hexagonales.
 * Proporciona endpoints para crear, consultar, actualizar y eliminar mapas y hexágonos.
 */
@RestController
@RequestMapping("/api/maps")
public class GameMapController {

    private final GameMapService mapService;

    @Autowired
    public GameMapController(GameMapService mapService) {
        this.mapService = mapService;
    }

    /**
     * Obtiene todos los mapas disponibles.
     *
     * @return Flujo de todos los mapas
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<GameMap> getAllMaps() {
        return mapService.getAllMaps();
    }

    /**
     * Busca mapas por nombre (búsqueda parcial).
     *
     * @param name Nombre o parte del nombre a buscar
     * @return Flujo de mapas que coinciden con el criterio de búsqueda
     */
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<GameMap> searchMaps(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String author) {
        if (name != null && !name.isEmpty()) {
            return mapService.findMapsByName(name);
        } else if (author != null && !author.isEmpty()) {
            return mapService.findMapsByAuthor(author);
        } else {
            return mapService.getAllMaps();
        }
    }

    /**
     * Obtiene un mapa por su ID.
     *
     * @param id ID del mapa
     * @return Mono con el mapa encontrado
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> getMapById(@PathVariable String id) {
        return mapService.getMapById(id);
    }

    /**
     * Crea un nuevo mapa.
     *
     * @param map Mapa a crear
     * @return Mono con el mapa creado
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> createMap(@Valid @RequestBody GameMap map) {
        return mapService.createMap(map);
    }

    /**
     * Actualiza un mapa existente.
     *
     * @param id ID del mapa a actualizar
     * @param map Datos actualizados del mapa
     * @return Mono con el mapa actualizado
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> updateMap(@PathVariable String id, @Valid @RequestBody GameMap map) {
        return mapService.updateMap(id, map);
    }

    /**
     * Elimina un mapa por su ID.
     *
     * @param id ID del mapa a eliminar
     * @return Mono vacío que completa cuando se elimina el mapa
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMap(@PathVariable String id) {
        return mapService.deleteMap(id);
    }

    /**
     * Genera un mapa hexagonal vacío con dimensiones específicas.
     *
     * @param name Nombre del mapa
     * @param description Descripción del mapa
     * @param author Autor del mapa
     * @param radius Radio del mapa (número de anillos de hexágonos desde el centro)
     * @param terrainType Tipo de terreno predeterminado para los hexágonos
     * @return Mono con el mapa generado
     */
    @PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> generateMap(@RequestParam String name,
                                    @RequestParam String description,
                                    @RequestParam String author,
                                    @RequestParam int radius,
                                    @RequestParam(defaultValue = "plain") String terrainType) {
        return mapService.generateEmptyHexagonalMap(name, description, author, radius, terrainType);
    }

    /**
     * Obtiene un hexágono de un mapa por sus coordenadas.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @return Mono con el hexágono encontrado
     */
    @GetMapping(path = "/{mapId}/tiles/{q}/{r}/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HexTile> getTileFromMap(@PathVariable String mapId,
                                       @PathVariable int q,
                                       @PathVariable int r,
                                       @PathVariable int s) {
        return mapService.getTileFromMap(mapId, q, r, s);
    }

    /**
     * Añade un hexágono a un mapa existente.
     *
     * @param mapId ID del mapa
     * @param tile Hexágono a añadir
     * @return Mono con el mapa actualizado
     */
    @PostMapping(path = "/{mapId}/tiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> addTileToMap(@PathVariable String mapId, @Valid @RequestBody HexTile tile) {
        return mapService.addTileToMap(mapId, tile);
    }

    /**
     * Actualiza un hexágono en un mapa.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @param tile Datos actualizados del hexágono
     * @return Mono con el mapa actualizado
     */
    @PutMapping(path = "/{mapId}/tiles/{q}/{r}/{s}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> updateTileInMap(@PathVariable String mapId,
                                        @PathVariable int q,
                                        @PathVariable int r,
                                        @PathVariable int s,
                                        @Valid @RequestBody HexTile tile) {
        return mapService.updateTileInMap(mapId, q, r, s, tile);
    }

    /**
     * Elimina un hexágono de un mapa.
     *
     * @param mapId ID del mapa
     * @param q Coordenada q del hexágono
     * @param r Coordenada r del hexágono
     * @param s Coordenada s del hexágono
     * @return Mono con el mapa actualizado
     */
    @DeleteMapping(path = "/{mapId}/tiles/{q}/{r}/{s}")
    public Mono<GameMap> removeTileFromMap(@PathVariable String mapId,
                                          @PathVariable int q,
                                          @PathVariable int r,
                                          @PathVariable int s) {
        return mapService.removeTileFromMap(mapId, q, r, s);
    }
}
