package org.ttarena.arena_user.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.ArmorType;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;
import org.ttarena.arena_user.model.enums.WarriorSpecialization;

@Setter
@Getter
@Document(collection = "characters")
public class Warrior extends Character {
    
    private WarriorSpecialization specialization;
    private int strength;
    
    public Warrior() {
        super();
    }
    
    public Warrior(String name, int health, int rage, WarriorSpecialization specialization) {
        super(name, health, rage, PowerResourceType.RAGE, CharacterClass.WARRIOR, ArmorType.PLATE);
        this.specialization = specialization;

        switch (specialization) {
            case ARMS:
                this.strength = 100;
                break;
            case FURY:
                this.strength = 120;
                break;
            case PROTECTION:
                this.strength = 80;
                break;
            default:
                this.strength = 90;
        }
    }

    @Override
    public String toString() {
        return "Warrior{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", rage=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", strength=" + strength +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
