package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.ArmorType;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;

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
    private CharacterClass characterClass;
    private ArmorType armorType;
    @Setter
    private int armor;
    
    public Character() {
    }
    
    public Character(String name, int health, int powerResourceAmount, 
                    PowerResourceType powerResourceType, CharacterClass characterClass) {
        this.name = name;
        this.health = health;
        this.powerResourceAmount = powerResourceAmount;
        this.powerResourceType = powerResourceType;
        setCharacterClass(characterClass);
    }

    public void setCharacterClass(CharacterClass characterClass) {
        this.characterClass = characterClass;

        ArmorType characterArmorType = determineArmorType(characterClass);
        if (characterArmorType != null) {
            setArmorType(characterArmorType);
        }
    }

    private ArmorType determineArmorType(CharacterClass characterClass) {
        if (characterClass == null) {
            return null;
        }

        return switch (characterClass) {
            case WARRIOR, PALADIN -> ArmorType.PLATE;
            case PRIEST -> ArmorType.CLOTH;
            case ROGUE -> ArmorType.LEATHER;
            case SHAMAN -> ArmorType.MAIL;
            default -> null;
        };
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
        if (armorType != null) {
            this.armor = armorType.getBaseValue();
        }
    }

    @Override
    public String toString() {
        return "Character{" +
                "id='" + id + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + health +
                ", powerResourceAmount=" + powerResourceAmount +
                ", powerResourceType=" + powerResourceType +
                ", characterClass=" + characterClass +
                ", armorType=" + armorType +
                ", armor=" + armor +
                '}';
    }
}
