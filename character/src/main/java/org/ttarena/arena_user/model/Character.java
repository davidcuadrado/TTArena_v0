package org.ttarena.arena_user.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.ArmorType;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;

@Getter
@Document(collection = "characters")
public abstract class Character {
    
    @Setter
    @Id
    private String id;
    @Setter
    private String name;
    @Setter
    private int health;
    @Setter
    private int powerResourceAmount;
    @Setter
    private PowerResourceType powerResourceType;
    @Setter
    private CharacterClass characterClass;
    private ArmorType armorType;
    @Setter
    private int armor;
    
    public Character() {
    }
    
    public Character(String name, int health, int powerResourceAmount, 
                    PowerResourceType powerResourceType, CharacterClass characterClass,
                    ArmorType armorType) {
        this.name = name;
        this.health = health;
        this.powerResourceAmount = powerResourceAmount;
        this.powerResourceType = powerResourceType;
        this.characterClass = characterClass;
        this.armorType = armorType;
        this.armor = armorType.getBaseValue();
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
        this.armor = armorType.getBaseValue();
    }

    @Override
    public String toString() {
        return "Character{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", health=" + health +
                ", powerResourceAmount=" + powerResourceAmount +
                ", powerResourceType=" + powerResourceType +
                ", characterClass=" + characterClass +
                ", armorType=" + armorType +
                ", armor=" + armor +
                '}';
    }
}
