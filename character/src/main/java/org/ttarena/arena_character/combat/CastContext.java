package org.ttarena.arena_character.combat;

import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.Character;

import java.util.List;

public record CastContext(Character caster, Ability ability, List<String> targetIds, String callerId) {
}
