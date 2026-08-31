package org.ttarena.arena_character.exception;

import java.io.Serial;

public class BadRequestException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}
}
