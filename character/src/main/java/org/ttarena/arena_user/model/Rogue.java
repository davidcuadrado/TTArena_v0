package org.ttarena.arena_user.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.ArmorType;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;
import org.ttarena.arena_user.model.enums.RogueSpecialization;

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
        super(name, health, energy, PowerResourceType.ENERGY, CharacterClass.ROGUE, ArmorType.LEATHER);
        this.specialization = specialization;

        switch (specialization) {
            case SUBTLETY:
                this.agility = 110;
                this.criticalStrike = 90;
                break;
            case ASSASSINATION:
                this.agility = 100;
                this.criticalStrike = 100;
                break;
            case OUTLAW:
                this.agility = 90;
                this.criticalStrike = 110;
                break;
            default:
                this.agility = 100;
                this.criticalStrike = 100;
        }
    }

    @Override
    public String toString() {
        return "Rogue{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", energy=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", agility=" + agility +
                ", criticalStrike=" + criticalStrike +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
