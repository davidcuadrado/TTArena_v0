package org.ttarena.arena_ability.model;

import lombok.Getter;

@Getter
public enum AbilityType {
    OFFENSIVE("Offensive"),
    DEFENSIVE("Defensive"),
    HEALING("Healing"),
    UTILITY("Utility"),
    BUFF("Buff"),
    DEBUFF("Debuff");

    private final String displayName;

    AbilityType(String displayName) {
        this.displayName = displayName;
    }

}

