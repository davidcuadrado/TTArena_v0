package org.ttarena.arena_character.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_character.model.enums.ArmorType;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.StatType;

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
    private int maxHealth;
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
        this.maxHealth = health;
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
            case WARRIOR, PALADIN, DEATH_KNIGHT -> ArmorType.PLATE;
            case PRIEST, MAGE, WARLOCK -> ArmorType.CLOTH;
            case ROGUE, DRUID, MONK, DEMON_HUNTER -> ArmorType.LEATHER;
            case SHAMAN, HUNTER, EVOKER -> ArmorType.MAIL;
            default -> null;
        };
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
        if (armorType != null) {
            this.armor = armorType.getBaseValue();
        }
    }

    /**
     * Returns the value of the given stat for this character, or 0 if the
     * stat does not apply to this class. Lets ability resolution scale
     * damage/healing off whichever stat the caster actually has without
     * needing to know the concrete subclass.
     */
    public abstract int getStatValue(StatType statType);

    /**
     * Applies damage, clamping health at 0. Returns the amount actually
     * dealt (may be less than requested if it would overkill).
     */
    public int applyDamage(int amount) {
        int actual = Math.min(amount, this.health);
        this.health = Math.max(0, this.health - amount);
        return Math.max(actual, 0);
    }

    /**
     * Applies healing, clamping health at maxHealth. Returns the amount
     * actually healed (may be less than requested if already near full).
     */
    public int applyHealing(int amount) {
        int before = this.health;
        this.health = Math.min(this.maxHealth, this.health + amount);
        return this.health - before;
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    @Override
    public String toString() {
        return "Character{" +
                "id='" + id + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + health +
                ", maxHealth=" + maxHealth +
                ", powerResourceAmount=" + powerResourceAmount +
                ", powerResourceType=" + powerResourceType +
                ", characterClass=" + characterClass +
                ", armorType=" + armorType +
                ", armor=" + armor +
                '}';
    }
}

