package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.HunterSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Hunter extends Character {
    private HunterSpecialization specialization;
    private int agility;
    private int criticalStrike;

    public Hunter() {
        super();
    }

    public Hunter(String name, int health, int focus, HunterSpecialization specialization) {
        super(name, health, focus, PowerResourceType.FOCUS, CharacterClass.HUNTER);
        this.specialization = specialization;

        this.agility = specialization.getBaseStats().getOrDefault(StatType.AGILITY, 0);
        this.criticalStrike = specialization.getBaseStats().getOrDefault(StatType.CRITICAL_STRIKE, 0);
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case AGILITY -> agility;
            case CRITICAL_STRIKE -> criticalStrike;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Hunter{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", focus=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", agility=" + agility +
                ", criticalStrike=" + criticalStrike +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
