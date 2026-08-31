package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.MonkSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Monk extends Character {

    private MonkSpecialization specialization;
    private int agility;
    private int spirit;

    public Monk() {
        super();
    }

    public Monk(String name, int health, int energy, MonkSpecialization specialization) {
        super(name, health, energy, PowerResourceType.ENERGY, CharacterClass.MONK);
        this.specialization = specialization;

        switch (specialization) {
            case BREWMASTER:
                this.agility = 90;
                this.spirit = 110;
                break;
            case MISTWEAVER:
                this.agility = 70;
                this.spirit = 130;
                break;
            case WINDWALKER:
                this.agility = 120;
                this.spirit = 70;
                break;
            default:
                this.agility = 100;
                this.spirit = 100;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case AGILITY -> agility;
            case SPIRIT -> spirit;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Monk{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", energy=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", agility=" + agility +
                ", spirit=" + spirit +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
