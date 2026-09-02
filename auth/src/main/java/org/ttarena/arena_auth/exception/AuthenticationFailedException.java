package org.ttarena.arena_auth.exception;

import java.io.Serial;

/** The credentials were rejected by the user service. */
public class AuthenticationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
