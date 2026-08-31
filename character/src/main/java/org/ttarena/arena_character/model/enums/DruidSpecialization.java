package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum DruidSpecialization {
    BALANCE(Role.DAMAGE),
    FERAL(Role.DAMAGE),
    GUARDIAN(Role.TANK),
    RESTORATION(Role.HEALER);

    private final Role role;

    DruidSpecialization(Role role) {
        this.role = role;
    }
}
