package org.ttarena.arena_user.model.enums;


public enum ArmorType {
    CLOTH(50),
    LEATHER(100),
    MAIL(150),
    PLATE(200);
    
    private final int baseValue;
    
    ArmorType(int baseValue) {
        this.baseValue = baseValue;
    }
    
    public int getBaseValue() {
        return baseValue;
    }
}
