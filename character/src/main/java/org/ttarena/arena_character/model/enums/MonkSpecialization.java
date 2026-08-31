package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum MonkSpecialization {
    BREWMASTER(Role.TANK),
    MISTWEAVER(Role.HEALER),
    WINDWALKER(Role.DAMAGE);

    private final Role role;

    MonkSpecialization(Role role) {
        this.role = role;
    }
}
