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

    private String specialization;

    private AbilityType abilityType;
    private TargetType targetType;

    private PowerResourceType resourceType;
    private int resourceCost;

    private int cooldownTurns;

    private int range;

    private int basePower;
    private StatType scalingStat;
    private double scalingFactor;

    public int computeEffectAmount(Character caster) {
        int statValue = caster.getStatValue(scalingStat);
        return (int) Math.round(basePower + (statValue * scalingFactor));
    }
}
