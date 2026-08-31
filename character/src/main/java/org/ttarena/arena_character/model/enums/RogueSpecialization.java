package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum RogueSpecialization {
    SUBTLETY(Role.DAMAGE),
    ASSASSINATION(Role.DAMAGE),
    OUTLAW(Role.DAMAGE);

    private final Role role;

    RogueSpecialization(Role role) {
        this.role = role;
    }
}
