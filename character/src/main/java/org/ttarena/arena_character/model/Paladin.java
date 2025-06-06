package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Paladin extends Character {
    
    private PaladinSpecialization specialization;
    private int strength;
    private int intellect;
    
    public Paladin() {
        super();
    }
    
    public Paladin(String name, int health, int holyPower, PaladinSpecialization specialization) {
        super(name, health, holyPower, PowerResourceType.HOLY_POWER, CharacterClass.PALADIN);
        this.specialization = specialization;

        switch (specialization) {
            case PROTECTION:
                this.strength = 90;
                this.intellect = 40;
                break;
            case HOLY:
                this.strength = 50;
                this.intellect = 100;
                break;
            case RETRIBUTION:
                this.strength = 110;
                this.intellect = 30;
                break;
            default:
                this.strength = 80;
                this.intellect = 60;
        }
    }


    @Override
    public String toString() {
        return "Paladin{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", holyPower=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", strength=" + strength +
                ", intellect=" + intellect +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
