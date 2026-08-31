package org.ttarena.arena_character.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.TargetType;

/**
 * A single spec-defining ability a character can cast in combat.
 * Kept as flat data (rather than a class hierarchy like Character) since
 * every ability, regardless of class, boils down to the same shape:
 * spend a resource, hit some targets, scale off a stat.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "abilities")
public class Ability {

    @Id
    private String id;

    private String name;
    private String description;

    private CharacterClass characterClass;
    /** Name of the specialization enum constant this ability belongs to, e.g. "ARMS". */
    private String specialization;

    private AbilityType abilityType;
    private TargetType targetType;

    private PowerResourceType resourceType;
    private int resourceCost;

    /** Informational for now: turns before the ability can be cast again. Not yet enforced. */
    private int cooldownTurns;

    private int basePower;
    private StatType scalingStat;
    private double scalingFactor;

    /**
     * Computes the raw effect magnitude (damage or healing) this ability
     * deals when cast by the given caster, before any target-side mitigation.
     */
    public int computeEffectAmount(Character caster) {
        int statValue = caster.getStatValue(scalingStat);
        return (int) Math.round(basePower + (statValue * scalingFactor));
    }
}
