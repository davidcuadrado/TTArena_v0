package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.model.enums.AbilityType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Indexes the available {@link AbilityEffect} beans by ability type.
 *
 * <p>Unlike {@link org.ttarena.arena_character.factory.CharacterFactoryRegistry}
 * this deliberately does not require full coverage: BUFF and DEBUFF are valid
 * ability types with no implementation yet, and a cast of one of those resolves
 * to "no effect" rather than failing.
 */
@Component
public class AbilityEffectRegistry {

    private final Map<AbilityType, AbilityEffect> effects = new EnumMap<>(AbilityType.class);

    public AbilityEffectRegistry(List<AbilityEffect> availableEffects) {
        for (AbilityEffect effect : availableEffects) {
            AbilityEffect previous = effects.put(effect.appliesTo(), effect);
            if (previous != null) {
                throw new IllegalStateException("Two effects claim " + effect.appliesTo() + ": "
                        + previous.getClass().getName() + " and " + effect.getClass().getName());
            }
        }
    }

    public Optional<AbilityEffect> forType(AbilityType abilityType) {
        return Optional.ofNullable(effects.get(abilityType));
    }
}
