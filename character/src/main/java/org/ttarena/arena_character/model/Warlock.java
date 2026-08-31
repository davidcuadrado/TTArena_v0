package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.WarlockSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

@Setter
@Getter
@Document(collection = "characters")
public class Warlock extends Character {

    private WarlockSpecialization specialization;
    private int intellect;
    private int spirit;

    public Warlock() {
        super();
    }

    public Warlock(String name, int health, int mana, WarlockSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.WARLOCK);
        this.specialization = specialization;

        switch (specialization) {
            case AFFLICTION:
                this.intellect = 110;
                this.spirit = 100;
                break;
            case DEMONOLOGY:
                this.intellect = 100;
                this.spirit = 90;
                break;
            case DESTRUCTION:
                this.intellect = 115;
                this.spirit = 80;
                break;
            default:
                this.intellect = 100;
                this.spirit = 90;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case INTELLECT -> intellect;
            case SPIRIT -> spirit;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Warlock{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", mana=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", intellect=" + intellect +
                ", spirit=" + spirit +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
