package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum WarlockSpecialization {
    AFFLICTION(Role.DAMAGE),
    DEMONOLOGY(Role.DAMAGE),
    DESTRUCTION(Role.DAMAGE);

    private final Role role;

    WarlockSpecialization(Role role) {
        this.role = role;
    }
}
