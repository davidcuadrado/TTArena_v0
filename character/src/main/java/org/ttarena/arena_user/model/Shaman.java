package org.ttarena.arena_user.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;
import org.ttarena.arena_user.model.enums.ShamanSpecialization;

@Setter
@Getter
@Document(collection = "characters")
public class Shaman extends Character {
    
    private ShamanSpecialization specialization;
    private int intellect;
    private int agility;
    
    public Shaman() {
        super();
    }
    
    public Shaman(String name, int health, int mana, ShamanSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.SHAMAN);
        this.specialization = specialization;

        switch (specialization) {
            case RESTORATION:
                this.intellect = 120;
                this.agility = 40;
                break;
            case ENHANCEMENT:
                this.intellect = 60;
                this.agility = 100;
                break;
            case ELEMENTAL:
                this.intellect = 110;
                this.agility = 50;
                break;
            default:
                this.intellect = 90;
                this.agility = 70;
        }
    }

    @Override
    public String toString() {
        return "Shaman{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", mana=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", intellect=" + intellect +
                ", agility=" + agility +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
