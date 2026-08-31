package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum ShamanSpecialization {
    RESTORATION(Role.HEALER),
    ENHANCEMENT(Role.DAMAGE),
    ELEMENTAL(Role.DAMAGE);

    private final Role role;

    ShamanSpecialization(Role role) {
        this.role = role;
    }
}
