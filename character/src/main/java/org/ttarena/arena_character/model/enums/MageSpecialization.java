package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum MageSpecialization {
    ARCANE(Role.DAMAGE),
    FIRE(Role.DAMAGE),
    FROST(Role.DAMAGE);

    private final Role role;

    MageSpecialization(Role role) {
        this.role = role;
    }
}
