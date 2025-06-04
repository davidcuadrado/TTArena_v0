package org.ttarena.arena_map.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HexTileNotFoundException extends RuntimeException {
    
    public HexTileNotFoundException(String mapId, String coordinates) {
        super("No se encontró el hexágono con coordenadas " + coordinates + " en el mapa con ID: " + mapId);
    }
}
