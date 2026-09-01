package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum WarlockSpecialization implements Specialization {

    AFFLICTION(Role.DAMAGE, Map.of(StatType.INTELLECT, 110, StatType.SPIRIT, 100)),
    DEMONOLOGY(Role.DAMAGE, Map.of(StatType.INTELLECT, 100, StatType.SPIRIT, 90)),
    DESTRUCTION(Role.DAMAGE, Map.of(StatType.INTELLECT, 115, StatType.SPIRIT, 80));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    WarlockSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
