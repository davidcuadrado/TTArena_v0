package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum ShamanSpecialization implements Specialization {
    RESTORATION(Role.HEALER, Map.of(StatType.INTELLECT, 120, StatType.AGILITY, 40)),
    ENHANCEMENT(Role.DAMAGE, Map.of(StatType.INTELLECT, 60, StatType.AGILITY, 100)),
    ELEMENTAL(Role.DAMAGE, Map.of(StatType.INTELLECT, 110, StatType.AGILITY, 50));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    ShamanSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
