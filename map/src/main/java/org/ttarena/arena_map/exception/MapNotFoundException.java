package org.ttarena.arena_map.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MapNotFoundException extends RuntimeException {
    
    public MapNotFoundException(String id) {
        super("No se encontró el mapa con ID: " + id);
    }
}
