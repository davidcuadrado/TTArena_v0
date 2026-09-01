package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.BadRequestException;

@Component
public class TargetsRequiredRule implements CastRule {

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void check(CastContext context) {
        if (context.targetIds() == null || context.targetIds().isEmpty()) {
            throw new BadRequestException(context.ability().getName() + " requires at least one target.");
        }
    }
}
