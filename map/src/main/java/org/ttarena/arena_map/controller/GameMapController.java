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

@RestController
@RequestMapping("/api/maps")
public class GameMapController {

    private final GameMapService mapService;

    @Autowired
    public GameMapController(GameMapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<GameMap> getAllMaps() {
        return mapService.getAllMaps();
    }

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

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> getMapById(@PathVariable String id) {
        return mapService.getMapById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> createMap(@Valid @RequestBody GameMap map) {
        return mapService.createMap(map);
    }


    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> updateMap(@PathVariable String id, @Valid @RequestBody GameMap map) {
        return mapService.updateMap(id, map);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMap(@PathVariable String id) {
        return mapService.deleteMap(id);
    }

    @PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> generateMap(@RequestParam String name,
                                    @RequestParam String description,
                                    @RequestParam String author,
                                    @RequestParam int radius,
                                    @RequestParam(defaultValue = "plain") String terrainType) {
        return mapService.generateEmptyHexagonalMap(name, description, author, radius, terrainType);
    }

    @GetMapping(path = "/{mapId}/tiles/{q}/{r}/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HexTile> getTileFromMap(@PathVariable String mapId,
                                       @PathVariable int q,
                                       @PathVariable int r,
                                       @PathVariable int s) {
        return mapService.getTileFromMap(mapId, q, r, s);
    }

    @PostMapping(path = "/{mapId}/tiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> addTileToMap(@PathVariable String mapId, @Valid @RequestBody HexTile tile) {
        return mapService.addTileToMap(mapId, tile);
    }

    @PutMapping(path = "/{mapId}/tiles/{q}/{r}/{s}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> updateTileInMap(@PathVariable String mapId,
                                        @PathVariable int q,
                                        @PathVariable int r,
                                        @PathVariable int s,
                                        @Valid @RequestBody HexTile tile) {
        return mapService.updateTileInMap(mapId, q, r, s, tile);
    }

    @DeleteMapping(path = "/{mapId}/tiles/{q}/{r}/{s}")
    public Mono<GameMap> removeTileFromMap(@PathVariable String mapId,
                                          @PathVariable int q,
                                          @PathVariable int r,
                                          @PathVariable int s) {
        return mapService.removeTileFromMap(mapId, q, r, s);
    }
}
