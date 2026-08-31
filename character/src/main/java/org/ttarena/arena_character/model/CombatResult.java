package org.ttarena.arena_character.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ttarena.arena_character.model.enums.AbilityType;

import java.util.List;

/**
 * Result of resolving a single ability cast. Not persisted - this is the
 * response payload the API returns for a POST /cast call.
 */
@Getter
@AllArgsConstructor
public class CombatResult {
    private String casterId;
    private String casterName;
    private String abilityId;
    private String abilityName;
    private AbilityType abilityType;
    private int resourceSpent;
    private int casterRemainingResource;
    private List<TargetOutcome> outcomes;
}
