package org.ttarena.arena_auth.exception;

import java.io.Serial;

/** The user service answered, but with a failure of its own. */
public class UserServiceFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserServiceFailedException(String message) {
        super(message);
    }
}
