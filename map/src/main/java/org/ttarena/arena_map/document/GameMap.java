package org.ttarena.arena_map.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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

    @Indexed
    private String ownerId;

    private String name;
    private String description;
    private int radius;
    private Instant createdAt;
    private Instant updatedAt;

    private Map<String, HexTile> tiles = new LinkedHashMap<>();

    public Collection<HexTile> allTiles() {
        return tiles.values();
    }

    public int tileCount() {
        return tiles.size();
    }

    public void putTile(HexTile tile) {
        tiles.put(tile.coordinate().key(), tile);
    }

    public HexTile tileAt(HexCoordinate coordinate) {
        return tiles.get(coordinate.key());
    }

    public boolean removeTile(HexCoordinate coordinate) {
        return tiles.remove(coordinate.key()) != null;
    }
}
