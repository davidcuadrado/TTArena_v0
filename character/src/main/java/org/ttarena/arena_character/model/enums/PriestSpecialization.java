package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum PriestSpecialization {
    HOLY(Role.HEALER),
    DISCIPLINE(Role.HEALER),
    SHADOW(Role.DAMAGE);

    private final Role role;

    PriestSpecialization(Role role) {
        this.role = role;
    }
}
