package org.ttarena.arena_ability.model;

import lombok.Getter;

@Getter
public enum WowClass {
    PALADIN("Paladin"),
    PRIEST("Priest"),
    ROGUE("Rogue"),
    SHAMAN("Shaman"),
    WARRIOR("Warrior");

    private final String displayName;

    WowClass(String displayName) {
        this.displayName = displayName;
    }

}

