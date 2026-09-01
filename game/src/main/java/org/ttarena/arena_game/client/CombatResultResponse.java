package org.ttarena.arena_game.client;

import java.util.List;

/**
 * The character service's cast response, narrowed to what a session needs.
 * Unknown fields are ignored, so the character service can add to it freely.
 */
public record CombatResultResponse(
        String casterId,
        String casterName,
        String abilityId,
        String abilityName,
        String abilityType,
        int resourceSpent,
        int casterRemainingResource,
        List<TargetOutcomeResponse> outcomes) {

    public record TargetOutcomeResponse(
            String targetId,
            String targetName,
            int amount,
            int resultingHealth,
            boolean defeated) {
    }
}
