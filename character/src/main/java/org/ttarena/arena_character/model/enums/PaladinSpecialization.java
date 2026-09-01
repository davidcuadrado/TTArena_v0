package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum PaladinSpecialization implements Specialization {

    PROTECTION(Role.TANK, Map.of(StatType.STRENGTH, 90, StatType.INTELLECT, 40)),
    HOLY(Role.HEALER, Map.of(StatType.STRENGTH, 50, StatType.INTELLECT, 100)),
    RETRIBUTION(Role.DAMAGE, Map.of(StatType.STRENGTH, 110, StatType.INTELLECT, 30));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    PaladinSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
