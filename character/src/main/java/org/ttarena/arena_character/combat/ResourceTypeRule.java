package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.BadRequestException;

@Component
public class ResourceTypeRule implements CastRule {

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void check(CastContext context) {
        if (context.ability().getResourceType() != context.caster().getPowerResourceType()) {
            throw new BadRequestException(
                    context.caster().getName() + " cannot use " + context.ability().getName()
                            + ": it costs " + context.ability().getResourceType()
                            + " but this character uses " + context.caster().getPowerResourceType() + ".");
        }
    }
}
