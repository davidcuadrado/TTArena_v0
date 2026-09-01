package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum EvokerSpecialization implements Specialization {

    DEVASTATION(Role.DAMAGE, Map.of(StatType.INTELLECT, 115, StatType.SPIRIT, 75)),
    PRESERVATION(Role.HEALER, Map.of(StatType.INTELLECT, 100, StatType.SPIRIT, 110)),
    AUGMENTATION(Role.DAMAGE, Map.of(StatType.INTELLECT, 105, StatType.SPIRIT, 90));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    EvokerSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
