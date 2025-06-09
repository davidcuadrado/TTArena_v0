package org.ttarena.arena_map.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexTile {
    

    private HexCoordinate coordinate;
    private String terrainType;
    private int elevation;
    private boolean passable;
    private int movementCost;

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    public HexTile addProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
        return this;
    }

    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }
}
