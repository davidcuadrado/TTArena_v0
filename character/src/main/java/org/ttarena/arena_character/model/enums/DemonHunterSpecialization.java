package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum DemonHunterSpecialization implements Specialization {
    HAVOC(Role.DAMAGE, Map.of(StatType.AGILITY, 120, StatType.STRENGTH, 60)),
    VENGEANCE(Role.TANK, Map.of(StatType.AGILITY, 90, StatType.STRENGTH, 100));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    DemonHunterSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
