package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.ForbiddenException;

@Component
public class CasterOwnershipRule implements CastRule {

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void check(CastContext context) {
        String ownerId = context.caster().getOwnerId();
        if (ownerId == null || !ownerId.equals(context.callerId())) {
            throw new ForbiddenException(
                    context.caster().getName() + " does not belong to this account.");
        }
    }
}
