package org.ttarena.arena_map.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.dto.ArenaDocument;
import org.ttarena.arena_map.dto.CreateMapRequest;
import org.ttarena.arena_map.dto.GenerateMapRequest;
import org.ttarena.arena_map.dto.PathResponse;
import org.ttarena.arena_map.dto.PlaceTileRequest;
import org.ttarena.arena_map.dto.UpdateMapRequest;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.repository.MapSummary;
import org.ttarena.arena_map.security.CurrentUser;
import org.ttarena.arena_map.security.CurrentUserProvider;
import org.ttarena.arena_map.service.GameMapService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/maps")
public class GameMapController {

    private final GameMapService mapService;
    private final CurrentUserProvider currentUserProvider;

    public GameMapController(GameMapService mapService, CurrentUserProvider currentUserProvider) {
        this.mapService = mapService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MapSummary> searchMaps(@RequestParam(required = false) String name) {
        return mapService.searchByName(name);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MapSummary> myMaps() {
        return currentUserProvider.currentUser()
                .map(CurrentUser::userId)
                .flatMapMany(mapService::mapsOwnedBy);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> getMap(@PathVariable String id) {
        return mapService.getById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> createMap(@Valid @RequestBody CreateMapRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.create(request, currentUser.userId()));
    }

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> generateMap(@Valid @RequestBody GenerateMapRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.generate(request, currentUser.userId()));
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GameMap> importArena(@Valid @RequestBody ArenaDocument arena) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.importArena(arena, currentUser.userId()));
    }

    @GetMapping(value = "/{id}/grid", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ArenaDocument> exportArena(@PathVariable String id) {
        return mapService.exportArena(id);
    }

    @PutMapping(value = "/{id}/grid", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> replaceArena(@PathVariable String id, @Valid @RequestBody ArenaDocument arena) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.replaceArena(id, currentUser.userId(), arena));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> updateMap(@PathVariable String id, @Valid @RequestBody UpdateMapRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.update(id, currentUser.userId(), request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMap(@PathVariable String id) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> mapService.delete(id, currentUser.userId()));
    }

    @GetMapping(value = "/{id}/tiles/{q}/{r}/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HexTile> getTile(@PathVariable String id,
                                 @PathVariable int q,
                                 @PathVariable int r,
                                 @PathVariable int s) {
        return mapService.tileAt(id, new HexCoordinate(q, r, s));
    }

    @PutMapping(value = "/{id}/tiles/{q}/{r}/{s}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> placeTile(@PathVariable String id,
                                   @PathVariable int q,
                                   @PathVariable int r,
                                   @PathVariable int s,
                                   @Valid @RequestBody PlaceTileRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser ->
                        mapService.placeTile(id, currentUser.userId(), new HexCoordinate(q, r, s), request));
    }

    @DeleteMapping(value = "/{id}/tiles/{q}/{r}/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GameMap> removeTile(@PathVariable String id,
                                    @PathVariable int q,
                                    @PathVariable int r,
                                    @PathVariable int s) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser ->
                        mapService.removeTile(id, currentUser.userId(), new HexCoordinate(q, r, s)));
    }

    @GetMapping(value = "/{id}/starting-positions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<HexCoordinate>> startingPositions(@PathVariable String id,
                                                       @RequestParam(defaultValue = "2") int count) {
        return mapService.startingPositions(id, count);
    }

    @GetMapping(value = "/{id}/path", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PathResponse> findPath(@PathVariable String id,
                                       @RequestParam String from,
                                       @RequestParam String to) {
        return mapService.findPath(id, HexCoordinate.parse(from), HexCoordinate.parse(to));
    }
}
