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

        this.intellect = specialization.getBaseStats().getOrDefault(StatType.INTELLECT, 0);
        this.spirit = specialization.getBaseStats().getOrDefault(StatType.SPIRIT, 0);
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
