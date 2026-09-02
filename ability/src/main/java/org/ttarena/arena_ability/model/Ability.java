package org.ttarena.arena_ability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "abilities")
public class Ability {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String iconUrl;
    
    private int cooldown;
    private int castTime;
    private int resourceCost;
    private ResourceType resourceType;
    
    private AbilityType abilityType;

    private WowClass wowClass;

    private Specialization specialization;

    private int baseValue;

    private int range;

    private boolean areaEffect;

    private int areaRadius;
}

