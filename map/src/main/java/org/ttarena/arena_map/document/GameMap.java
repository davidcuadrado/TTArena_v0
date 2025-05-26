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

/**
 * Documento principal que representa un mapa de juego compuesto por hexágonos.
 * Esta clase se persiste en MongoDB y contiene toda la información necesaria
 * para representar un mapa completo en el juego Arena.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "maps")
public class GameMap {
    
    /**
     * Identificador único del mapa
     */
    @Id
    private String id;
    
    /**
     * Nombre del mapa
     */
    private String name;
    
    /**
     * Descripción del mapa
     */
    private String description;
    
    /**
     * Autor o creador del mapa
     */
    private String author;
    
    /**
     * Fecha de creación del mapa
     */
    private LocalDateTime createdAt;
    
    /**
     * Fecha de última modificación del mapa
     */
    private LocalDateTime updatedAt;
    
    /**
     * Ancho del mapa en número de hexágonos
     */
    private int width;
    
    /**
     * Alto del mapa en número de hexágonos
     */
    private int height;
    
    /**
     * Colección de hexágonos que componen el mapa.
     * La clave es una representación de string de las coordenadas (formato "q:r:s")
     * y el valor es el hexágono correspondiente.
     */
    @Builder.Default
    private Map<String, HexTile> tiles = new HashMap<>();
    
    /**
     * Metadatos adicionales del mapa
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Añade un hexágono al mapa
     * 
     * @param tile El hexágono a añadir
     * @return El propio mapa para permitir encadenamiento de métodos
     */
    public GameMap addTile(HexTile tile) {
        if (tiles == null) {
            tiles = new HashMap<>();
        }
        
        HexCoordinate coord = tile.getCoordinate();
        String key = String.format("%d:%d:%d", coord.getQ(), coord.getR(), coord.getS());
        tiles.put(key, tile);
        return this;
    }
    
    /**
     * Obtiene un hexágono del mapa por sus coordenadas
     * 
     * @param coordinate Las coordenadas del hexágono
     * @return El hexágono correspondiente o null si no existe
     */
    public HexTile getTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return null;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.get(key);
    }
    
    /**
     * Comprueba si existe un hexágono en las coordenadas especificadas
     * 
     * @param coordinate Las coordenadas a comprobar
     * @return true si existe un hexágono en esas coordenadas, false en caso contrario
     */
    public boolean hasTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return false;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.containsKey(key);
    }
    
    /**
     * Elimina un hexágono del mapa
     * 
     * @param coordinate Las coordenadas del hexágono a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    public boolean removeTile(HexCoordinate coordinate) {
        if (tiles == null) {
            return false;
        }
        
        String key = String.format("%d:%d:%d", coordinate.getQ(), coordinate.getR(), coordinate.getS());
        return tiles.remove(key) != null;
    }
    
    /**
     * Añade un metadato al mapa
     * 
     * @param key Clave del metadato
     * @param value Valor del metadato
     * @return El propio mapa para permitir encadenamiento de métodos
     */
    public GameMap addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }
}
