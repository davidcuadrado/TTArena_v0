package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.BadRequestException;

@Component
public class ResourceCostRule implements CastRule {

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void check(CastContext context) {
        if (context.caster().getPowerResourceAmount() < context.ability().getResourceCost()) {
            throw new BadRequestException(
                    context.caster().getName() + " does not have enough " + context.ability().getResourceType()
                            + " to cast " + context.ability().getName()
                            + " (needs " + context.ability().getResourceCost()
                            + ", has " + context.caster().getPowerResourceAmount() + ").");
        }
    }
}
