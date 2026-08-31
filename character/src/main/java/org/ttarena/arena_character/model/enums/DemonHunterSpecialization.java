package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum DemonHunterSpecialization {
    HAVOC(Role.DAMAGE),
    VENGEANCE(Role.TANK);

    private final Role role;

    DemonHunterSpecialization(Role role) {
        this.role = role;
    }
}
