package org.ttarena.arena_character.exception;

import java.io.Serial;

/**
 * Unchecked, for the same reason as {@link BadRequestException}: a checked
 * exception cannot be thrown from inside a reactive operator's lambda.
 * {@code GlobalExceptionHandler} turns it into a 404.
 */
public class NotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}

}
