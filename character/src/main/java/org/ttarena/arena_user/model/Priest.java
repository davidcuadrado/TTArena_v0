package org.ttarena.arena_user.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;
import org.ttarena.arena_user.model.enums.PriestSpecialization;

@Setter
@Getter
@Document(collection = "characters")
public class Priest extends Character {
    
    private PriestSpecialization specialization;
    private int intellect;
    private int spirit;
    
    public Priest() {
        super();
    }
    
    public Priest(String name, int health, int mana, PriestSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.PRIEST);
        this.specialization = specialization;

        switch (specialization) {
            case HOLY:
                this.intellect = 90;
                this.spirit = 110;
                break;
            case DISCIPLINE:
                this.intellect = 100;
                this.spirit = 100;
                break;
            case SHADOW:
                this.intellect = 120;
                this.spirit = 80;
                break;
            default:
                this.intellect = 100;
                this.spirit = 100;
        }
    }

    @Override
    public String toString() {
        return "Priest{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", mana=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", intellect=" + intellect +
                ", spirit=" + spirit +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
