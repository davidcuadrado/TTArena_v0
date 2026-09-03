package org.ttarena.arena_character.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * RFC 7807 problem responses, the same shape the auth service returns, so a
 * client parses one error format across the whole system.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_TYPE = "https://ttarena.org/problems/";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "not-found", "Not found", e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(ForbiddenException e) {
        return problem(HttpStatus.FORBIDDEN, "not-your-character", "Not your character", e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException e) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", e.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ProblemDetail> handleDatabase(DatabaseException e) {
        log.error("Database failure: {}", e.getMessage());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "storage-failure", "Storage failure", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrentModification(OptimisticLockingFailureException e) {
        log.warn("Concurrent modification: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "concurrent-modification", "Changed by someone else",
                "This character was changed while you were acting on it. Fetch it again and retry.");
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleValidation(WebExchangeBindException e) {
        String detail = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", detail);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDetail> handleMalformedInput(ServerWebInputException e) {
        return problem(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed request", e.getReason());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", e.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(PROBLEM_TYPE + type));
        problem.setTitle(title);
        if (detail != null && !detail.isBlank()) {
            problem.setDetail(detail);
        }
        return ResponseEntity.status(status).body(problem);
    }
}
