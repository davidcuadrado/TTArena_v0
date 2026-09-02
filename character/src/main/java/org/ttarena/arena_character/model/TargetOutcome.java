package org.ttarena.arena_character.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TargetOutcome {
    private String targetId;
    private String targetName;
    private int amount;
    private int resultingHealth;
    private boolean defeated;
}
