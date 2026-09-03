package org.ttarena.arena_map.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_map.model.HexCoordinate;
import org.ttarena.arena_map.model.HexTile;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Document(collection = "maps")
public class GameMap {

    @Id
    private String id;

    @Version
    private Long version;

    @Indexed
    private String ownerId;

    private String name;
    private String description;
    private int radius;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Stored rather than derived so that listing maps can leave the tiles in the
     * database. Every route that changes the tiles goes through the methods
     * below, which keep it true.
     */
    @Setter(AccessLevel.NONE)
    private int tileCount;

    @Setter(AccessLevel.NONE)
    private Map<String, HexTile> tiles = new LinkedHashMap<>();

    public Collection<HexTile> allTiles() {
        return tiles.values();
    }

    public void putTile(HexTile tile) {
        tiles.put(tile.coordinate().key(), tile);
        tileCount = tiles.size();
    }

    public HexTile tileAt(HexCoordinate coordinate) {
        return tiles.get(coordinate.key());
    }

    public boolean removeTile(HexCoordinate coordinate) {
        boolean removed = tiles.remove(coordinate.key()) != null;
        tileCount = tiles.size();
        return removed;
    }

    public void clearTiles() {
        tiles.clear();
        tileCount = 0;
    }

    public boolean holds(HexCoordinate coordinate) {
        return coordinate.ringIndex() <= radius;
    }
}
