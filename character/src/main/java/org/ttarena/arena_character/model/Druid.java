package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DruidSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Druid extends Character {

    private DruidSpecialization specialization;
    private int intellect;
    private int agility;

    public Druid() {
        super();
    }

    public Druid(String name, int health, int mana, DruidSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.DRUID);
        this.specialization = specialization;

        switch (specialization) {
            case BALANCE:
                this.intellect = 120;
                this.agility = 50;
                break;
            case FERAL:
                this.intellect = 50;
                this.agility = 120;
                break;
            case GUARDIAN:
                this.intellect = 60;
                this.agility = 100;
                break;
            case RESTORATION:
                this.intellect = 110;
                this.agility = 60;
                break;
            default:
                this.intellect = 90;
                this.agility = 90;
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
        return "Druid{" +
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
