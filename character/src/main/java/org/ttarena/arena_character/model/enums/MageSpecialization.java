package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum MageSpecialization implements Specialization {

    ARCANE(Role.DAMAGE, Map.of(StatType.INTELLECT, 120, StatType.CRITICAL_STRIKE, 70)),
    FIRE(Role.DAMAGE, Map.of(StatType.INTELLECT, 100, StatType.CRITICAL_STRIKE, 110)),
    FROST(Role.DAMAGE, Map.of(StatType.INTELLECT, 105, StatType.CRITICAL_STRIKE, 90));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    MageSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
