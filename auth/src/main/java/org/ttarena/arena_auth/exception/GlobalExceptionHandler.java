package org.ttarena.arena_auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * RFC 7807 responses for everything except a rejected login, which stays
 * deliberately bare: a 401 that explains itself is a 401 that helps someone
 * work out which half of the credentials was wrong.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailed(AuthenticationFailedException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create("https://ttarena.org/problems/invalid-credentials"));
        problem.setTitle("Invalid credentials");
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleUnavailable(UserServiceUnavailableException e) {
        log.error("User service unreachable: {}", e.getMessage());

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setType(URI.create("https://ttarena.org/problems/user-service-unavailable"));
        problem.setTitle("Sign-in is temporarily unavailable");
        problem.setDetail("The user service could not be reached. Try again shortly.");
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    @ExceptionHandler(UserServiceFailedException.class)
    public ResponseEntity<ProblemDetail> handleUserServiceFailure(UserServiceFailedException e) {
        log.error("User service returned a failure: {}", e.getMessage());

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problem.setType(URI.create("https://ttarena.org/problems/user-service-failed"));
        problem.setTitle("Sign-in failed upstream");
        problem.setDetail("The user service reported an error.");
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleValidation(WebExchangeBindException e) {
        String detail = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ttarena.org/problems/invalid-request"));
        problem.setTitle("Invalid request");
        problem.setDetail(detail);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }
}
