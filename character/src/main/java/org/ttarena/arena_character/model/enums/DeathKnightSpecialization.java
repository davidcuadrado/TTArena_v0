package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum DeathKnightSpecialization {
    BLOOD(Role.TANK),
    FROST(Role.DAMAGE),
    UNHOLY(Role.DAMAGE);

    private final Role role;

    DeathKnightSpecialization(Role role) {
        this.role = role;
    }
}
