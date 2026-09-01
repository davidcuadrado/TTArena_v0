package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.AbilityType;

@Component
public class DamageEffect implements AbilityEffect {
    private static final double ARMOR_CONSTANT = 400.0;

    private static final double MAX_MITIGATION = 0.75;

    private static final int MINIMUM_DAMAGE = 1;

    @Override
    public AbilityType appliesTo() {
        return AbilityType.DAMAGE;
    }

    @Override
    public int apply(Character target, int amount) {
        double mitigation = Math.min(MAX_MITIGATION, target.getArmor() / (target.getArmor() + ARMOR_CONSTANT));
        int mitigatedAmount = (int) Math.round(amount * (1 - mitigation));
        return target.applyDamage(Math.max(mitigatedAmount, MINIMUM_DAMAGE));
    }
}
