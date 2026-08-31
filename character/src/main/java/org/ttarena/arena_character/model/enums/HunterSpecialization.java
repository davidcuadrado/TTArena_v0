package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum HunterSpecialization {
    BEAST_MASTERY(Role.DAMAGE),
    MARKSMANSHIP(Role.DAMAGE),
    SURVIVAL(Role.DAMAGE);

    private final Role role;

    HunterSpecialization(Role role) {
        this.role = role;
    }
}
