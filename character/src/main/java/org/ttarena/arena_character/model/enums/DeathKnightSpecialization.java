package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum DeathKnightSpecialization implements Specialization {

    BLOOD(Role.TANK, Map.of(StatType.STRENGTH, 110, StatType.CRITICAL_STRIKE, 70)),
    FROST(Role.DAMAGE, Map.of(StatType.STRENGTH, 100, StatType.CRITICAL_STRIKE, 100)),
    UNHOLY(Role.DAMAGE, Map.of(StatType.STRENGTH, 90, StatType.CRITICAL_STRIKE, 110));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    DeathKnightSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
