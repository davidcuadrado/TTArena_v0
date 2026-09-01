package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum MonkSpecialization implements Specialization {

    BREWMASTER(Role.TANK, Map.of(StatType.AGILITY, 90, StatType.SPIRIT, 110)),
    MISTWEAVER(Role.HEALER, Map.of(StatType.AGILITY, 70, StatType.SPIRIT, 130)),
    WINDWALKER(Role.DAMAGE, Map.of(StatType.AGILITY, 120, StatType.SPIRIT, 70));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    MonkSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
