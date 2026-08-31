package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum PaladinSpecialization {
    PROTECTION(Role.TANK),
    HOLY(Role.HEALER),
    RETRIBUTION(Role.DAMAGE);

    private final Role role;

    PaladinSpecialization(Role role) {
        this.role = role;
    }
}
