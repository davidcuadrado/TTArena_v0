package org.ttarena.arena_map.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa un hexágono individual dentro de un mapa.
 * Cada hexágono tiene una coordenada única y puede contener propiedades
 * como tipo de terreno, objetos, personajes, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexTile {
    
    /**
     * Coordenada única del hexágono en el sistema de coordenadas cúbicas
     */
    private HexCoordinate coordinate;
    
    /**
     * Tipo de terreno del hexágono (ej: agua, tierra, montaña, etc.)
     */
    private String terrainType;
    
    /**
     * Nivel de elevación del hexágono
     */
    private int elevation;
    
    /**
     * Indica si el hexágono es transitable
     */
    private boolean passable;
    
    /**
     * Coste de movimiento para atravesar este hexágono
     */
    private int movementCost;
    
    /**
     * Propiedades adicionales del hexágono almacenadas como pares clave-valor
     */
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
    
    /**
     * Añade una propiedad al hexágono
     * 
     * @param key Clave de la propiedad
     * @param value Valor de la propiedad
     * @return El propio hexágono para permitir encadenamiento de métodos
     */
    public HexTile addProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
        return this;
    }
    
    /**
     * Obtiene una propiedad del hexágono
     * 
     * @param key Clave de la propiedad
     * @return Valor de la propiedad o null si no existe
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }
}
