package org.ttarena.arena_character.model.enums;

import lombok.Getter;

@Getter
public enum EvokerSpecialization {
    DEVASTATION(Role.DAMAGE),
    PRESERVATION(Role.HEALER),
    AUGMENTATION(Role.DAMAGE);

    private final Role role;

    EvokerSpecialization(Role role) {
        this.role = role;
    }
}
