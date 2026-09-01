package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.AbilityType;

@Component
public class HealEffect implements AbilityEffect {
    @Override
    public AbilityType appliesTo() {
        return AbilityType.HEAL;
    }

    @Override
    public int apply(Character target, int amount) {
        return target.applyHealing(amount);
    }
}
