package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum WarriorSpecialization {
    ARMS(Role.DAMAGE),
    FURY(Role.DAMAGE),
    PROTECTION(Role.TANK);

    private final Role role;

    WarriorSpecialization(Role role) {
        this.role = role;
    }
}
