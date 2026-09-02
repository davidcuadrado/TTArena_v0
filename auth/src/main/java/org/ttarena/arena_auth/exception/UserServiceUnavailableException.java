package org.ttarena.arena_auth.exception;

import java.io.Serial;

/** The user service could not be reached, or did not answer in time. */
public class UserServiceUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
