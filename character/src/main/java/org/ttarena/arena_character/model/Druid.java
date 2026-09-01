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

        this.intellect = specialization.getBaseStats().getOrDefault(StatType.INTELLECT, 0);
        this.agility = specialization.getBaseStats().getOrDefault(StatType.AGILITY, 0);
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
