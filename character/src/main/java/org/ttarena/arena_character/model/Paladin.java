package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
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

        this.strength = specialization.getBaseStats().getOrDefault(StatType.STRENGTH, 0);
        this.intellect = specialization.getBaseStats().getOrDefault(StatType.INTELLECT, 0);
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case STRENGTH -> strength;
            case INTELLECT -> intellect;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Paladin{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", holyPower=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", strength=" + strength +
                ", intellect=" + intellect +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
