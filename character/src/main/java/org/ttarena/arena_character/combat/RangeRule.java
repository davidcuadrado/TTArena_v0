package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.BadRequestException;

@Component
public class RangeRule implements CastRule {

    @Override
    public int order() {
        return 45;
    }

    @Override
    public void check(CastContext context) {
        Integer distance = context.distanceToTarget();
        if (distance == null) {
            return;
        }
        int range = context.ability().getRange();
        if (distance > range) {
            throw new BadRequestException("%s reaches %d %s; the target is %d away."
                    .formatted(context.ability().getName(), range, range == 1 ? "hex" : "hexes", distance));
        }
    }
}
