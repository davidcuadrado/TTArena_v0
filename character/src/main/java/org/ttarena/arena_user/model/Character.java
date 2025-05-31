package org.ttarena.arena_user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.PowerResourceType;

/**
 * Clase base para todos los personajes.
 * Utiliza discriminadores para la herencia en MongoDB.
 */
@Document(collection = "characters")
public abstract class Character {
    
    @Id
    private String id;
    
    private String name;
    private int health;
    private int powerResourceAmount;
    private PowerResourceType powerResourceType;
    private CharacterClass characterClass;
    
    public Character() {
        // Constructor vacío requerido por MongoDB
    }
    
    public Character(String name, int health, int powerResourceAmount, 
                    PowerResourceType powerResourceType, CharacterClass characterClass) {
        this.name = name;
        this.health = health;
        this.powerResourceAmount = powerResourceAmount;
        this.powerResourceType = powerResourceType;
        this.characterClass = characterClass;
    }
    
    // Getters y setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getPowerResourceAmount() {
        return powerResourceAmount;
    }
    
    public void setPowerResourceAmount(int powerResourceAmount) {
        this.powerResourceAmount = powerResourceAmount;
    }
    
    public PowerResourceType getPowerResourceType() {
        return powerResourceType;
    }
    
    public void setPowerResourceType(PowerResourceType powerResourceType) {
        this.powerResourceType = powerResourceType;
    }
    
    public CharacterClass getCharacterClass() {
        return characterClass;
    }
    
    public void setCharacterClass(CharacterClass characterClass) {
        this.characterClass = characterClass;
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
                '}';
    }
}
