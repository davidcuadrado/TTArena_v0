package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

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

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case INTELLECT -> intellect;
            case AGILITY -> agility;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Shaman{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", mana=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", intellect=" + intellect +
                ", agility=" + agility +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
