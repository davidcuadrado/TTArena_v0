package org.ttarena.arena_map.util;

import org.ttarena.arena_map.document.GameMap;
import org.ttarena.arena_map.document.HexCoordinate;
import org.ttarena.arena_map.document.HexTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidad para operaciones con mapas hexagonales.
 * Proporciona métodos para generar y manipular mapas hexagonales.
 */
public class MapGenerator {

    /**
     * Genera un mapa hexagonal con un patrón de terreno aleatorio.
     *
     * @param name Nombre del mapa
     * @param description Descripción del mapa
     * @param author Autor del mapa
     * @param radius Radio del mapa (número de anillos de hexágonos desde el centro)
     * @return El mapa generado
     */
    public static GameMap generateRandomMap(String name, String description, String author, int radius) {
        GameMap map = new GameMap();
        map.setName(name);
        map.setDescription(description);
        map.setAuthor(author);
        map.setWidth(radius * 2 + 1);
        map.setHeight(radius * 2 + 1);
        
        String[] terrainTypes = {"agua", "llanura", "bosque", "montaña", "desierto"};
        
        // Generar hexágonos en un patrón circular con el radio especificado
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            
            for (int r = r1; r <= r2; r++) {
                int s = -q - r;
                HexCoordinate coord = new HexCoordinate(q, r, s);
                
                // Seleccionar un tipo de terreno aleatorio
                String terrainType = terrainTypes[(int)(Math.random() * terrainTypes.length)];
                
                // Determinar si es transitable y el coste de movimiento según el tipo de terreno
                boolean passable = !terrainType.equals("montaña") && !terrainType.equals("agua");
                int movementCost = switch (terrainType) {
                    case "llanura" -> 1;
                    case "bosque" -> 2;
                    case "desierto" -> 3;
                    default -> 99; // No transitable
                };
                
                // Determinar elevación basada en la distancia al centro
                int distanceToCenter = Math.max(Math.abs(q), Math.max(Math.abs(r), Math.abs(s)));
                int elevation = terrainType.equals("montaña") ? 
                    5 + (int)(Math.random() * 5) : // Montañas más altas
                    distanceToCenter / 2 + (int)(Math.random() * 3); // Elevación gradual
                
                HexTile tile = HexTile.builder()
                        .coordinate(coord)
                        .terrainType(terrainType)
                        .elevation(elevation)
                        .passable(passable)
                        .movementCost(movementCost)
                        .build();
                
                map.addTile(tile);
            }
        }
        
        return map;
    }
    
    /**
     * Calcula la ruta más corta entre dos hexágonos utilizando el algoritmo A*.
     *
     * @param map El mapa
     * @param start Coordenada de inicio
     * @param goal Coordenada de destino
     * @return Lista de coordenadas que forman la ruta, o lista vacía si no hay ruta posible
     */
    public static List<HexCoordinate> findPath(GameMap map, HexCoordinate start, HexCoordinate goal) {
        // Implementación básica de A* para encontrar caminos en mapas hexagonales
        // Esta es una versión simplificada que podría expandirse según necesidades
        
        // Si el inicio o destino no son transitables, no hay ruta
        HexTile startTile = map.getTile(start);
        HexTile goalTile = map.getTile(goal);
        
        if (startTile == null || goalTile == null || !startTile.isPassable() || !goalTile.isPassable()) {
            return new ArrayList<>();
        }
        
        // Si inicio y destino son el mismo, devolver solo esa posición
        if (start.equals(goal)) {
            List<HexCoordinate> path = new ArrayList<>();
            path.add(start);
            return path;
        }
        
        // Aquí iría la implementación completa del algoritmo A*
        // Por simplicidad, devolvemos una ruta directa (esto debería reemplazarse por A* real)
        List<HexCoordinate> path = new ArrayList<>();
        path.add(start);
        path.add(goal);
        
        return path;
    }
    
    /**
     * Calcula el campo de visión desde un hexágono específico.
     *
     * @param map El mapa
     * @param center Coordenada central desde donde se calcula la visión
     * @param radius Radio de visión (en número de hexágonos)
     * @return Lista de coordenadas visibles desde el centro
     */
    public static List<HexCoordinate> calculateLineOfSight(GameMap map, HexCoordinate center, int radius) {
        List<HexCoordinate> visibleHexes = new ArrayList<>();
        
        // Añadir el hexágono central
        visibleHexes.add(center);
        
        // Comprobar todos los hexágonos dentro del radio
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            
            for (int r = r1; r <= r2; r++) {
                int s = -q - r;
                
                // Saltar el hexágono central
                if (q == 0 && r == 0 && s == 0) continue;
                
                HexCoordinate coord = new HexCoordinate(center.getQ() + q, center.getR() + r, center.getS() + s);
                
                // Comprobar si el hexágono está en el mapa
                if (map.hasTile(coord)) {
                    // Comprobar línea de visión (simplificado)
                    // En una implementación real, se comprobaría si hay obstáculos entre el centro y este hexágono
                    visibleHexes.add(coord);
                }
            }
        }
        
        return visibleHexes;
    }
}
