package org.ttarena.arena_character.exception;

import java.io.Serial;

/**
 * Unchecked so it can be thrown from inside lambdas and reactive operators.
 *
 * <p>A checked exception cannot cross a {@code map}/{@code flatMap} boundary,
 * which is why it can only ever be handed to {@code Mono.error(...)}. Validation
 * that happens deeper down - a factory rejecting an unknown specialization, say
 * - needs to be able to just throw. {@code GlobalExceptionHandler} still turns
 * it into a 400 either way.
 */
public class BadRequestException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}
}
