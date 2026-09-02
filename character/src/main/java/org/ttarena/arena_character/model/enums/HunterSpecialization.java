package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum HunterSpecialization implements Specialization {
    BEAST_MASTERY(Role.DAMAGE, Map.of(StatType.AGILITY, 110, StatType.CRITICAL_STRIKE, 80)),
    MARKSMANSHIP(Role.DAMAGE, Map.of(StatType.AGILITY, 90, StatType.CRITICAL_STRIKE, 120)),
    SURVIVAL(Role.DAMAGE, Map.of(StatType.AGILITY, 100, StatType.CRITICAL_STRIKE, 100));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    HunterSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
