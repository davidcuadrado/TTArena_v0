package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
import org.ttarena.arena_character.model.enums.Role;
import org.ttarena.arena_character.model.enums.StatType;

@Setter
@Getter
@Document(collection = "characters")
public class Shaman extends Character {
    
    private ShamanSpecialization specialization;
    private int intellect;
    private int agility;
    
    public Shaman() {
        super();
    }
    
    public Shaman(String name, int health, int mana, ShamanSpecialization specialization) {
        super(name, health, mana, PowerResourceType.MANA, CharacterClass.SHAMAN);
        this.specialization = specialization;
        // Base stats come from the specialization itself - see Specialization.
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
        return "Shaman{" +
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
