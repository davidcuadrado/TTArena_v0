package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum PriestSpecialization implements Specialization {

    HOLY(Role.HEALER, Map.of(StatType.INTELLECT, 90, StatType.SPIRIT, 110)),
    DISCIPLINE(Role.HEALER, Map.of(StatType.INTELLECT, 100, StatType.SPIRIT, 100)),
    SHADOW(Role.DAMAGE, Map.of(StatType.INTELLECT, 120, StatType.SPIRIT, 80));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    PriestSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
