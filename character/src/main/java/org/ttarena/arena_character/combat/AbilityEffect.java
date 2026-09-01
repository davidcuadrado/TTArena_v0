package org.ttarena.arena_character.combat;

import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.AbilityType;

public interface AbilityEffect {
    AbilityType appliesTo();

    int apply(Character target, int amount);
}
