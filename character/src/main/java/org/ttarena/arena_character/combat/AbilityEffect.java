package org.ttarena.arena_character.combat;

import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.AbilityType;

/**
 * What an ability actually does to one target.
 *
 * <p>One implementation per {@link AbilityType}, collected by
 * {@link AbilityEffectRegistry}. Adding BUFF or DEBUFF support means adding an
 * implementation here rather than another branch inside the cast pipeline.
 */
public interface AbilityEffect {

    /** The ability type this effect handles. Must be unique across all effects. */
    AbilityType appliesTo();

    /**
     * Applies the effect to a target.
     *
     * @param target the character on the receiving end
     * @param amount the raw magnitude computed from the caster's stats, before
     *               any target-side mitigation this effect chooses to apply
     * @return the amount actually applied, for reporting back to the caller
     */
    int apply(Character target, int amount);
}
