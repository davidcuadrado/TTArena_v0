package org.ttarena.arena_character.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outcome of an ability's effect on a single target. Not persisted -
 * built fresh per cast and returned to the caller.
 */
@Getter
@AllArgsConstructor
public class TargetOutcome {
    private String targetId;
    private String targetName;
    private int amount;
    private int resultingHealth;
    private boolean defeated;
}
