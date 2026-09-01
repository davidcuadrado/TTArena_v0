package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.AbilityType;

@Component
public class DamageEffect implements AbilityEffect {

    /** Armor value at which mitigation reaches 50%. */
    private static final double ARMOR_CONSTANT = 400.0;

    /** No amount of armor mitigates more than this share of incoming damage. */
    private static final double MAX_MITIGATION = 0.75;

    /** However heavy the armor, a hit always lands for at least this much. */
    private static final int MINIMUM_DAMAGE = 1;

    @Override
    public AbilityType appliesTo() {
        return AbilityType.DAMAGE;
    }

    /**
     * Flat armor mitigation: armor / (armor + 400), capped at 75% reduction.
     * A plate-wearer (armor 200) mitigates ~33%, cloth (armor 50) ~11%.
     */
    @Override
    public int apply(Character target, int amount) {
        double mitigation = Math.min(MAX_MITIGATION, target.getArmor() / (target.getArmor() + ARMOR_CONSTANT));
        int mitigatedAmount = (int) Math.round(amount * (1 - mitigation));
        return target.applyDamage(Math.max(mitigatedAmount, MINIMUM_DAMAGE));
    }
}
