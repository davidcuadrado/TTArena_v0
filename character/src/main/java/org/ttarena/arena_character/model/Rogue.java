package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.RogueSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

@Setter
@Getter
@Document(collection = "characters")
public class Rogue extends Character {
    private RogueSpecialization specialization;
    private int agility;
    private int criticalStrike;

    public Rogue() {
        super();
    }

    public Rogue(String name, int health, int energy, RogueSpecialization specialization) {
        super(name, health, energy, PowerResourceType.ENERGY, CharacterClass.ROGUE);
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
        return "Rogue{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", energy=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", agility=" + agility +
                ", criticalStrike=" + criticalStrike +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
