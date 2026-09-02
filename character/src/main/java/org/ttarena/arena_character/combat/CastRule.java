package org.ttarena.arena_character.combat;

public interface CastRule {

    int order();

    void check(CastContext context);
}
