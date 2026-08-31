package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.MageSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Mage extends Character {

    private MageSpecialization specialization;
    private int intellect;
    private int criticalStrike;

    public Mage() {
        super();
    }

    public Mage(String name, int health, int mana, MageSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.MAGE);
        this.specialization = specialization;

        switch (specialization) {
            case ARCANE:
                this.intellect = 120;
                this.criticalStrike = 70;
                break;
            case FIRE:
                this.intellect = 100;
                this.criticalStrike = 110;
                break;
            case FROST:
                this.intellect = 105;
                this.criticalStrike = 90;
                break;
            default:
                this.intellect = 100;
                this.criticalStrike = 90;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case INTELLECT -> intellect;
            case CRITICAL_STRIKE -> criticalStrike;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "Mage{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", mana=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", intellect=" + intellect +
                ", criticalStrike=" + criticalStrike +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
