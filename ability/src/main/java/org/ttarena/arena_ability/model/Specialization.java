package org.ttarena.arena_ability.model;

import lombok.Getter;

@Getter
public enum Specialization {
    // Paladin specializations
    HOLY_PALADIN("Holy", WowClass.PALADIN),
    PROTECTION_PALADIN("Protection", WowClass.PALADIN),
    RETRIBUTION_PALADIN("Retribution", WowClass.PALADIN),
    
    // Priest specializations
    DISCIPLINE_PRIEST("Discipline", WowClass.PRIEST),
    HOLY_PRIEST("Holy", WowClass.PRIEST),
    SHADOW_PRIEST("Shadow", WowClass.PRIEST),
    
    // Rogue specializations
    ASSASSINATION_ROGUE("Assassination", WowClass.ROGUE),
    OUTLAW_ROGUE("Outlaw", WowClass.ROGUE),
    SUBTLETY_ROGUE("Subtlety", WowClass.ROGUE),
    
    // Shaman specializations
    ELEMENTAL_SHAMAN("Elemental", WowClass.SHAMAN),
    ENHANCEMENT_SHAMAN("Enhancement", WowClass.SHAMAN),
    RESTORATION_SHAMAN("Restoration", WowClass.SHAMAN),
    
    // Warrior specializations
    ARMS_WARRIOR("Arms", WowClass.WARRIOR),
    FURY_WARRIOR("Fury", WowClass.WARRIOR),
    PROTECTION_WARRIOR("Protection", WowClass.WARRIOR);

    private final String displayName;
    private final WowClass wowClass;

    Specialization(String displayName, WowClass wowClass) {
        this.displayName = displayName;
        this.wowClass = wowClass;
    }

}

