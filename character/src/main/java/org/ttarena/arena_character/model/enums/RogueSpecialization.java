package org.ttarena.arena_character.model.enums;

import lombok.Getter;

import java.util.Map;

@Getter
public enum RogueSpecialization implements Specialization {
    SUBTLETY(Role.DAMAGE, Map.of(StatType.AGILITY, 110, StatType.CRITICAL_STRIKE, 90)),
    ASSASSINATION(Role.DAMAGE, Map.of(StatType.AGILITY, 100, StatType.CRITICAL_STRIKE, 100)),
    OUTLAW(Role.DAMAGE, Map.of(StatType.AGILITY, 90, StatType.CRITICAL_STRIKE, 110));

    private final Role role;
    private final Map<StatType, Integer> baseStats;

    RogueSpecialization(Role role, Map<StatType, Integer> baseStats) {
        this.role = role;
        this.baseStats = baseStats;
    }
}
