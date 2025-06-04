package org.ttarena.arena_user.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;
import org.ttarena.arena_user.model.enums.WarriorSpecialization;

@Document(collection = "characters")
public class Warrior extends Character {
    
    private WarriorSpecialization specialization;
    private int strength;
    private int armor;
    
    public Warrior() {
        super();
    }
    
    public Warrior(String name, int health, int rage, WarriorSpecialization specialization) {
        super(name, health, rage, PowerResourceType.RAGE, CharacterClass.WARRIOR);
        this.specialization = specialization;

        switch (specialization) {
            case ARMS:
                this.strength = 100;
                this.armor = 50;
                break;
            case FURY:
                this.strength = 120;
                this.armor = 30;
                break;
            case PROTECTION:
                this.strength = 80;
                this.armor = 120;
                break;
            default:
                this.strength = 90;
                this.armor = 70;
        }
    }
    
    public WarriorSpecialization getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(WarriorSpecialization specialization) {
        this.specialization = specialization;
    }
    
    public int getStrength() {
        return strength;
    }
    
    public void setStrength(int strength) {
        this.strength = strength;
    }
    
    public int getArmor() {
        return armor;
    }
    
    public void setArmor(int armor) {
        this.armor = armor;
    }
    
    @Override
    public String toString() {
        return "Warrior{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", health=" + getHealth() +
                ", rage=" + getPowerResourceAmount() +
                ", specialization=" + specialization +
                ", strength=" + strength +
                ", armor=" + armor +
                '}';
    }
}
