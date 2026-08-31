package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

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
        super(name, health, rage, PowerResourceType.RAGE, CharacterClass.WARRIOR);
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
                // Podríamos añadir un bonus de armadura para Protection si fuera necesario
                break;
            default:
                this.strength = 90;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case STRENGTH -> strength;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Warrior{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", rage=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", strength=" + strength +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
