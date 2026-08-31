package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DeathKnightSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class DeathKnight extends Character {

    private DeathKnightSpecialization specialization;
    private int strength;
    private int criticalStrike;

    public DeathKnight() {
        super();
    }

    public DeathKnight(String name, int health, int runicPower, DeathKnightSpecialization specialization) {
        super(name, health, runicPower, PowerResourceType.RUNIC_POWER, CharacterClass.DEATH_KNIGHT);
        this.specialization = specialization;

        switch (specialization) {
            case BLOOD:
                this.strength = 110;
                this.criticalStrike = 70;
                break;
            case FROST:
                this.strength = 100;
                this.criticalStrike = 100;
                break;
            case UNHOLY:
                this.strength = 90;
                this.criticalStrike = 110;
                break;
            default:
                this.strength = 100;
                this.criticalStrike = 90;
        }
    }

    public Role getRole() {
        return specialization.getRole();
    }

    @Override
    public int getStatValue(StatType statType) {
        return switch (statType) {
            case STRENGTH -> strength;
            case CRITICAL_STRIKE -> criticalStrike;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "DeathKnight{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", runicPower=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", strength=" + strength +
                ", criticalStrike=" + criticalStrike +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
