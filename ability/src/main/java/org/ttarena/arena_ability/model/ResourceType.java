package org.ttarena.arena_ability.model;

import lombok.Getter;

@Getter
public enum ResourceType {
    MANA("Mana"),
    RAGE("Rage"),
    ENERGY("Energy"),
    FOCUS("Focus"),
    RUNIC_POWER("Runic Power"),
    HOLY_POWER("Holy Power"),
    NONE("None");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

}

