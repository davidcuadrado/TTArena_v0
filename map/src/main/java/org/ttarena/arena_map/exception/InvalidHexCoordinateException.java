package org.ttarena.arena_map.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidHexCoordinateException extends RuntimeException {
    
    public InvalidHexCoordinateException(String message) {
        super(message);
    }
    
    public InvalidHexCoordinateException(int q, int r, int s) {
        super(String.format("Coordenadas hexagonales inválidas (q=%d, r=%d, s=%d). Debe cumplirse q + r + s = 0", q, r, s));
    }
}
