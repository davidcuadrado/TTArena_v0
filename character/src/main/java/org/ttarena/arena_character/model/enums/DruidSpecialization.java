package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum DruidSpecialization implements Specialization {

    BALANCE(Role.DAMAGE, Map.of(StatType.INTELLECT, 120, StatType.AGILITY, 50)),
    FERAL(Role.DAMAGE, Map.of(StatType.INTELLECT, 50, StatType.AGILITY, 120)),
    GUARDIAN(Role.TANK, Map.of(StatType.INTELLECT, 60, StatType.AGILITY, 100)),
    RESTORATION(Role.HEALER, Map.of(StatType.INTELLECT, 110, StatType.AGILITY, 60));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    DruidSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
