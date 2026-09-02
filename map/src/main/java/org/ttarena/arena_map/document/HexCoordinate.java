package org.ttarena.arena_map.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HexCoordinate {
    private int q;
    private int r;
    private int s;

    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
        this.s = -q - r;
    }

    public boolean isValid() {
        return q + r + s == 0;
    }

    public int distanceTo(HexCoordinate other) {
        return (Math.abs(this.q - other.q) + Math.abs(this.r - other.r) + Math.abs(this.s - other.s)) / 2;
    }

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
