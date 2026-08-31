package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.EvokerSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.PowerResourceType;

@Setter
@Getter
@Document(collection = "characters")
public class Evoker extends Character {

    private EvokerSpecialization specialization;
    private int intellect;
    private int spirit;

    public Evoker() {
        super();
    }

    public Evoker(String name, int health, int essence, EvokerSpecialization specialization) {
        super(name, health, essence, PowerResourceType.ESSENCE, CharacterClass.EVOKER);
        this.specialization = specialization;

        switch (specialization) {
            case DEVASTATION:
                this.intellect = 115;
                this.spirit = 75;
                break;
            case PRESERVATION:
                this.intellect = 100;
                this.spirit = 110;
                break;
            case AUGMENTATION:
                this.intellect = 105;
                this.spirit = 90;
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
        return "Evoker{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", essence=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", role=" + getRole() +
                ", intellect=" + intellect +
                ", spirit=" + spirit +
                ", armorType=" + getArmorType() +
                ", armor=" + getArmor() +
                '}';
    }
}
