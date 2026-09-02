package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.model.enums.AbilityType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
