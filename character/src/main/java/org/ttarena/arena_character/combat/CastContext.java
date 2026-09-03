package org.ttarena.arena_character.combat;

import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.Character;

import java.util.List;

/**
 * {@code distanceToTarget} is null when the caller has no board to measure on
 * — the character service can be exercised on its own, and only the game
 * service knows where anybody is standing.
 */
public record CastContext(Character caster, Ability ability, List<String> targetIds, String callerId,
                          Integer distanceToTarget) {
}
