package org.ttarena.arena_map.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio de mapas.
 * Proporciona respuestas de error consistentes para las diferentes excepciones que pueden ocurrir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo MapNotFoundException.
     *
     * @param ex La excepción capturada
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(MapNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMapNotFoundException(MapNotFoundException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Mapa no encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de tipo HexTileNotFoundException.
     *
     * @param ex La excepción capturada
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(HexTileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHexTileNotFoundException(HexTileNotFoundException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Hexágono no encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de tipo InvalidHexCoordinateException.
     *
     * @param ex La excepción capturada
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(InvalidHexCoordinateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidHexCoordinateException(InvalidHexCoordinateException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Coordenadas hexagonales inválidas",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de validación de datos de entrada.
     *
     * @param ex La excepción capturada
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(WebExchangeBindException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        errorResponse.put("message", "Hay errores de validación en la solicitud");
        errorResponse.put("errors", validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción no controlada específicamente.
     *
     * @param ex La excepción capturada
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Crea una estructura de respuesta de error consistente.
     *
     * @param status Código de estado HTTP
     * @param error Tipo de error
     * @param message Mensaje detallado del error
     * @return Mapa con la estructura de error
     */
    private Map<String, Object> createErrorResponse(int status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
