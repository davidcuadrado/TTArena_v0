package org.ttarena.arena_map.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa las coordenadas de un hexágono en un sistema de coordenadas cúbicas.
 * En este sistema, cada hexágono se identifica por tres coordenadas (q, r, s) donde q + r + s = 0.
 * 
 * - q: Coordenada en el eje Q (de izquierda a derecha)
 * - r: Coordenada en el eje R (de arriba-izquierda a abajo-derecha)
 * - s: Coordenada en el eje S (de arriba-derecha a abajo-izquierda)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexCoordinate {
    private int q;
    private int r;
    private int s;
    
    /**
     * Constructor que calcula automáticamente la coordenada s basada en q y r.
     * 
     * @param q Coordenada en el eje Q
     * @param r Coordenada en el eje R
     */
    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
        this.s = -q - r;
    }
    
    /**
     * Valida que las coordenadas cumplan con la restricción q + r + s = 0.
     * 
     * @return true si las coordenadas son válidas, false en caso contrario
     */
    public boolean isValid() {
        return q + r + s == 0;
    }
    
    /**
     * Calcula la distancia entre esta coordenada y otra.
     * 
     * @param other La otra coordenada
     * @return La distancia en número de hexágonos
     */
    public int distanceTo(HexCoordinate other) {
        return (Math.abs(this.q - other.q) + Math.abs(this.r - other.r) + Math.abs(this.s - other.s)) / 2;
    }
    
    /**
     * Obtiene las coordenadas de los hexágonos vecinos.
     * 
     * @return Un array con las coordenadas de los seis hexágonos vecinos
     */
    public HexCoordinate[] getNeighbors() {
        return new HexCoordinate[] {
            new HexCoordinate(q + 1, r, s - 1), // Este
            new HexCoordinate(q + 1, r - 1, s), // Noreste
            new HexCoordinate(q, r - 1, s + 1), // Noroeste
            new HexCoordinate(q - 1, r, s + 1), // Oeste
            new HexCoordinate(q - 1, r + 1, s), // Suroeste
            new HexCoordinate(q, r + 1, s - 1)  // Sureste
        };
    }
}
