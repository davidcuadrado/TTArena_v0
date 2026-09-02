package org.ttarena.arena_user.exception;

import java.io.Serial;

public class DatabaseException extends Exception {
	@Serial
    private static final long serialVersionUID = 1L;

	public DatabaseException(String message) {
        super(message);
    }
}