package org.ttarena.arena_map.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.config.MapProperties;
import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.dto.CreateMapRequest;
import org.ttarena.arena_map.dto.GenerateMapRequest;
import org.ttarena.arena_map.dto.PlaceTileRequest;
import org.ttarena.arena_map.dto.UpdateMapRequest;
import org.ttarena.arena_map.exception.BadRequestException;
import org.ttarena.arena_map.exception.ForbiddenException;
import org.ttarena.arena_map.exception.NotFoundException;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;
import org.ttarena.arena_map.model.TerrainType;
import org.ttarena.arena_map.repository.GameMapRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameMapServiceTest {

    private static final String OWNER = "owner-1";
    private static final String INTRUDER = "owner-2";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private GameMapRepository repository;
    private GameMapService service;

    @BeforeEach
    void setUp() {
        repository = mock(GameMapRepository.class);
        service = new GameMapService(
                repository,
                new MapProperties(8, 2),
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(repository.save(any(GameMap.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private GameMap existingMap(String ownerId) {
        GameMap map = new GameMap();
        map.setId("map-1");
        map.setOwnerId(ownerId);
        map.setRadius(4);
        map.setName("Old name");
        map.setCreatedAt(NOW);
        map.setUpdatedAt(NOW);
        return map;
    }

    @Test
    void createStampsTheOwnerFromTheCallerAndNotTheRequest() {
        when(repository.countByOwnerId(OWNER)).thenReturn(Mono.just(0L));

        StepVerifier.create(service.create(new CreateMapRequest("Frozen Pass", "cold", 4), OWNER))
                .assertNext(map -> {
                    assertThat(map.getOwnerId()).isEqualTo(OWNER);
                    assertThat(map.getName()).isEqualTo("Frozen Pass");
                    assertThat(map.getCreatedAt()).isEqualTo(NOW);
                    assertThat(map.getUpdatedAt()).isEqualTo(NOW);
                })
                .verifyComplete();
    }

    @Test
    void createRefusesOnceTheOwnerQuotaIsReached() {
        when(repository.countByOwnerId(OWNER)).thenReturn(Mono.just(2L));

        StepVerifier.create(service.create(new CreateMapRequest("One Too Many", null, 2), OWNER))
                .expectError(BadRequestException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void generateRefusesARadiusAboveTheConfiguredMaximum() {
        StepVerifier.create(service.generate(new GenerateMapRequest("Huge", null, 9, TerrainType.PLAIN), OWNER))
                .expectError(BadRequestException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void generateFillsEveryTileOfTheRequestedRadius() {
        when(repository.countByOwnerId(OWNER)).thenReturn(Mono.just(0L));

        StepVerifier.create(service.generate(new GenerateMapRequest("Arena", null, 2, TerrainType.PLAIN), OWNER))
                .assertNext(map -> {
                    assertThat(map.getRadius()).isEqualTo(2);
                    assertThat(map.getTileCount()).isEqualTo(MapGenerator.tileCountFor(2));
                    assertThat(map.allTiles()).allSatisfy(tile ->
                            assertThat(tile.terrain()).isEqualTo(TerrainType.PLAIN));
                })
                .verifyComplete();
    }

    @Test
    void unknownMapIsReportedAsNotFound() {
        when(repository.findById(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(service.getById("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateIsRefusedForSomebodyElsesMap() {
        when(repository.findById("map-1")).thenReturn(Mono.just(existingMap(OWNER)));

        StepVerifier.create(service.update("map-1", INTRUDER, new UpdateMapRequest("Stolen", null)))
                .expectError(ForbiddenException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void deleteIsRefusedForSomebodyElsesMap() {
        when(repository.findById("map-1")).thenReturn(Mono.just(existingMap(OWNER)));

        StepVerifier.create(service.delete("map-1", INTRUDER))
                .expectError(ForbiddenException.class)
                .verify();

        verify(repository, never()).delete(any());
    }

    @Test
    void updateOnlyReplacesTheFieldsThatWereSent() {
        when(repository.findById("map-1")).thenReturn(Mono.just(existingMap(OWNER)));

        StepVerifier.create(service.update("map-1", OWNER, new UpdateMapRequest(null, "just a description")))
                .assertNext(map -> {
                    assertThat(map.getName()).isEqualTo("Old name");
                    assertThat(map.getDescription()).isEqualTo("just a description");
                })
                .verifyComplete();
    }

    @Test
    void placeTileWritesThroughTheCoordinateFromThePathNotTheBody() {
        when(repository.findById("map-1")).thenReturn(Mono.just(existingMap(OWNER)));
        HexCoordinate target = HexCoordinate.axial(1, -1);

        StepVerifier.create(service.placeTile("map-1", OWNER, target, new PlaceTileRequest(TerrainType.FOREST, 3)))
                .assertNext(map -> {
                    HexTile tile = map.tileAt(target);
                    assertThat(tile).isNotNull();
                    assertThat(tile.terrain()).isEqualTo(TerrainType.FOREST);
                    assertThat(tile.elevation()).isEqualTo(3);
                    assertThat(tile.coordinate()).isEqualTo(target);
                })
                .verifyComplete();
    }

    @Test
    void removingATileThatIsNotThereIsReportedAsNotFound() {
        when(repository.findById("map-1")).thenReturn(Mono.just(existingMap(OWNER)));

        StepVerifier.create(service.removeTile("map-1", OWNER, HexCoordinate.origin()))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void findPathReportsUnreachableWithoutFailing() {
        GameMap map = existingMap(OWNER);
        MapGenerator.fill(map, 2, TileFactory.uniform(TerrainType.WATER));
        when(repository.findById("map-1")).thenReturn(Mono.just(map));

        StepVerifier.create(service.findPath("map-1", HexCoordinate.origin(), HexCoordinate.axial(1, 0)))
                .assertNext(response -> {
                    assertThat(response.reachable()).isFalse();
                    assertThat(response.path()).isEmpty();
                    assertThat(response.movementCost()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void aTileOutsideTheArenaIsRefused() {
        GameMap map = existingMap(OWNER);
        map.setRadius(2);
        when(repository.findById("map-1")).thenReturn(Mono.just(map));

        StepVerifier.create(service.placeTile("map-1", OWNER, HexCoordinate.axial(5, 0),
                        new PlaceTileRequest(TerrainType.PLAIN, 0)))
                .expectError(BadRequestException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void aTileOnTheOuterRingIsAccepted() {
        GameMap map = existingMap(OWNER);
        map.setRadius(2);
        when(repository.findById("map-1")).thenReturn(Mono.just(map));

        StepVerifier.create(service.placeTile("map-1", OWNER, HexCoordinate.axial(2, 0),
                        new PlaceTileRequest(TerrainType.PLAIN, 0)))
                .assertNext(saved -> assertThat(saved.getTileCount()).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void theStoredTileCountFollowsEveryChange() {
        GameMap map = existingMap(OWNER);
        map.setRadius(2);

        map.putTile(new HexTile(HexCoordinate.origin(), TerrainType.PLAIN, 0));
        map.putTile(new HexTile(HexCoordinate.axial(1, 0), TerrainType.FOREST, 0));
        assertThat(map.getTileCount()).isEqualTo(2);

        map.putTile(new HexTile(HexCoordinate.origin(), TerrainType.WATER, 0));
        assertThat(map.getTileCount()).isEqualTo(2);

        map.removeTile(HexCoordinate.origin());
        assertThat(map.getTileCount()).isEqualTo(1);

        map.clearTiles();
        assertThat(map.getTileCount()).isZero();
    }

    @Test
    void creatingAnArenaLargerThanAllowedIsRefused() {
        StepVerifier.create(service.create(new CreateMapRequest("Vast", null, 9), OWNER))
                .expectError(BadRequestException.class)
                .verify();
    }
}
