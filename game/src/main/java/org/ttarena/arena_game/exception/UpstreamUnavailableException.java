package org.ttarena.arena_game.exception;

/** Another service did not answer in time, or could not be reached at all. */
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
