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
        // Base stats come from the specialization itself - see Specialization.
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
