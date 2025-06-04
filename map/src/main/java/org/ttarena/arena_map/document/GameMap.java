package org.ttarena.arena_map.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "maps")
public class GameMap {

    @Id
    private String id;
    private String name;
    private String description;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int width;
    private int height;

    @Builder.Default
    private Map<String, HexTile> tiles = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public GameMap addTile(HexTile tile) {
        if (tiles == null) {
            tiles = new HashMap<>();
        }
        HexCoordinate coord = tile.getCoordinate();
        String key = String.format("%d:%d:%d", coord.getQ(), coord.getR(), coord.getS());
        tiles.put(key, tile);
        return this;
    }

    public HexTile getTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return null;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.get(key);
    }

    public boolean hasTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return false;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.containsKey(key);
    }

    public boolean removeTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return false;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.remove(key) != null;
    }

    public GameMap addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }
}
