package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DemonHunterSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class DemonHunter extends Character {

    private DemonHunterSpecialization specialization;
    private int agility;
    private int strength;

    public DemonHunter() {
        super();
    }

    public DemonHunter(String name, int health, int fury, DemonHunterSpecialization specialization) {
        super(name, health, fury, PowerResourceType.FURY, CharacterClass.DEMON_HUNTER);
        this.specialization = specialization;

        switch (specialization) {
            case HAVOC:
                this.agility = 120;
                this.strength = 60;
                break;
            case VENGEANCE:
                this.agility = 90;
                this.strength = 100;
                break;
            default:
                this.agility = 100;
                this.strength = 80;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case AGILITY -> agility;
            case STRENGTH -> strength;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "DemonHunter{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", fury=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", agility=" + agility +
                ", strength=" + strength +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
