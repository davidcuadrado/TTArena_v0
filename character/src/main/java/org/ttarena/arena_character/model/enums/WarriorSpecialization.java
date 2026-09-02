package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum WarriorSpecialization implements Specialization {
    ARMS(Role.DAMAGE, Map.of(StatType.STRENGTH, 100)),
    FURY(Role.DAMAGE, Map.of(StatType.STRENGTH, 120)),
    PROTECTION(Role.TANK, Map.of(StatType.STRENGTH, 80));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    WarriorSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
