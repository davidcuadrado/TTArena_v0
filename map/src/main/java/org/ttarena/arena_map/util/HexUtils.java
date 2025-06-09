package org.ttarena.arena_map.util;

public class HexUtils {

    public static final int[][] DIRECTIONS = {
        {1, -1, 0},  // Este
        {1, 0, -1},  // Noreste
        {0, 1, -1},  // Noroeste
        {-1, 1, 0},  // Oeste
        {-1, 0, 1},  // Suroeste
        {0, -1, 1}   // Sureste
    };

    public static final String[] DIRECTION_NAMES = {
        "ESTE", "NORESTE", "NOROESTE", "OESTE", "SUROESTE", "SURESTE"
    };

    public static double[] cubeToPixel(int q, int r, double size) {
        double x = size * (3.0/2.0 * q);
        double y = size * (Math.sqrt(3)/2.0 * q + Math.sqrt(3) * r);
        return new double[]{x, y};
    }

    public static int[] pixelToCube(double x, double y, double size) {
        double q = (2.0/3.0 * x) / size;
        double r = (-1.0/3.0 * x + Math.sqrt(3)/3.0 * y) / size;
        
        // Redondeo para obtener la celda hexagonal más cercana
        return cubeRound(q, r, -q-r);
    }

    public static int[] cubeRound(double q, double r, double s) {
        int qi = (int)Math.round(q);
        int ri = (int)Math.round(r);
        int si = (int)Math.round(s);

        double q_diff = Math.abs(qi - q);
        double r_diff = Math.abs(ri - r);
        double s_diff = Math.abs(si - s);

        if (q_diff > r_diff && q_diff > s_diff) {
            qi = -ri - si;
        } else if (r_diff > s_diff) {
            ri = -qi - si;
        } else {
            si = -qi - ri;
        }

        return new int[]{qi, ri, si};
    }

    public static int distance(int q1, int r1, int s1, int q2, int r2, int s2) {
        return (Math.abs(q1 - q2) + Math.abs(r1 - r2) + Math.abs(s1 - s2)) / 2;
    }

    public static String getHexKey(int q, int r, int s) {
        return String.format("%d:%d:%d", q, r, s);
    }
}
