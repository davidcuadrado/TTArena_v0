package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

@Setter
@Getter
@Document(collection = "characters")
public class Priest extends Character {
    
    private PriestSpecialization specialization;
    private int intellect;
    private int spirit;
    
    public Priest() {
        super();
    }
    
    public Priest(String name, int health, int mana, PriestSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.PRIEST);
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
        return "Priest{" +
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
