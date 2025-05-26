package org.ttarena.arena_map.util;

/**
 * Clase de utilidad para operaciones con coordenadas hexagonales.
 * Proporciona métodos estáticos para manipular y calcular propiedades
 * relacionadas con el sistema de coordenadas cúbicas para hexágonos.
 */
public class HexUtils {

    /**
     * Direcciones para los seis vecinos de un hexágono.
     * Cada dirección es un vector de desplazamiento en coordenadas cúbicas.
     */
    public static final int[][] DIRECTIONS = {
        {1, -1, 0},  // Este
        {1, 0, -1},  // Noreste
        {0, 1, -1},  // Noroeste
        {-1, 1, 0},  // Oeste
        {-1, 0, 1},  // Suroeste
        {0, -1, 1}   // Sureste
    };

    /**
     * Nombres de las direcciones para referencia.
     */
    public static final String[] DIRECTION_NAMES = {
        "ESTE", "NORESTE", "NOROESTE", "OESTE", "SUROESTE", "SURESTE"
    };

    /**
     * Convierte coordenadas cúbicas (q,r,s) a coordenadas de píxeles (x,y).
     * Útil para renderizar hexágonos en una pantalla.
     *
     * @param q Coordenada q
     * @param r Coordenada r
     * @param size Tamaño del hexágono (distancia del centro a un vértice)
     * @return Array con las coordenadas [x, y]
     */
    public static double[] cubeToPixel(int q, int r, double size) {
        double x = size * (3.0/2.0 * q);
        double y = size * (Math.sqrt(3)/2.0 * q + Math.sqrt(3) * r);
        return new double[]{x, y};
    }

    /**
     * Convierte coordenadas de píxeles (x,y) a coordenadas cúbicas (q,r,s).
     * Útil para determinar qué hexágono se encuentra en una posición de pantalla.
     *
     * @param x Coordenada x
     * @param y Coordenada y
     * @param size Tamaño del hexágono (distancia del centro a un vértice)
     * @return Array con las coordenadas [q, r, s]
     */
    public static int[] pixelToCube(double x, double y, double size) {
        double q = (2.0/3.0 * x) / size;
        double r = (-1.0/3.0 * x + Math.sqrt(3)/3.0 * y) / size;
        
        // Redondeo para obtener la celda hexagonal más cercana
        return cubeRound(q, r, -q-r);
    }

    /**
     * Redondea coordenadas cúbicas fraccionarias al hexágono más cercano.
     *
     * @param q Coordenada q fraccionaria
     * @param r Coordenada r fraccionaria
     * @param s Coordenada s fraccionaria
     * @return Array con las coordenadas enteras [q, r, s]
     */
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

    /**
     * Calcula la distancia entre dos hexágonos en coordenadas cúbicas.
     *
     * @param q1 Coordenada q del primer hexágono
     * @param r1 Coordenada r del primer hexágono
     * @param s1 Coordenada s del primer hexágono
     * @param q2 Coordenada q del segundo hexágono
     * @param r2 Coordenada r del segundo hexágono
     * @param s2 Coordenada s del segundo hexágono
     * @return Distancia en número de hexágonos
     */
    public static int distance(int q1, int r1, int s1, int q2, int r2, int s2) {
        return (Math.abs(q1 - q2) + Math.abs(r1 - r2) + Math.abs(s1 - s2)) / 2;
    }

    /**
     * Genera una clave única para un hexágono basada en sus coordenadas.
     *
     * @param q Coordenada q
     * @param r Coordenada r
     * @param s Coordenada s
     * @return Clave única en formato "q:r:s"
     */
    public static String getHexKey(int q, int r, int s) {
        return String.format("%d:%d:%d", q, r, s);
    }
}
