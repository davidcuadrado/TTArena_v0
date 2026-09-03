package org.ttarena.arena_character.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.TargetType;
import org.ttarena.arena_character.repository.AbilityRepository;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("dev")
public class AbilityDataInitializer {
    @Bean
    public CommandLineRunner loadAbilities(AbilityRepository abilityRepository) {
        return args -> {
            abilityRepository.deleteAll().block();

            List<Ability> abilities = new ArrayList<>();

            abilities.add(ability("Mortal Strike", "A vicious strike that deals heavy physical damage.", CharacterClass.WARRIOR, "ARMS", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RAGE, 30, 0, 20, StatType.STRENGTH, 0.5, 1));
            abilities.add(ability("Execute", "A brutal finishing blow.", CharacterClass.WARRIOR, "ARMS", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RAGE, 20, 1, 15, StatType.STRENGTH, 0.4, 1));
            abilities.add(ability("Bloodthirst", "A furious strike fueled by rage.", CharacterClass.WARRIOR, "FURY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RAGE, 20, 0, 18, StatType.STRENGTH, 0.45, 1));
            abilities.add(ability("Whirlwind", "A spinning attack that hits every nearby enemy.", CharacterClass.WARRIOR, "FURY", AbilityType.DAMAGE, TargetType.ALL_ENEMIES, PowerResourceType.RAGE, 25, 1, 12, StatType.STRENGTH, 0.3, 1));
            abilities.add(ability("Shield Slam", "Slams the target with a shield.", CharacterClass.WARRIOR, "PROTECTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RAGE, 15, 0, 15, StatType.STRENGTH, 0.35, 1));
            abilities.add(ability("Revenge", "A retaliatory strike against a foe.", CharacterClass.WARRIOR, "PROTECTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RAGE, 20, 1, 14, StatType.STRENGTH, 0.3, 1));
            abilities.add(ability("Holy Light", "A bright light that mends wounds.", CharacterClass.PALADIN, "HOLY", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.HOLY_POWER, 25, 0, 25, StatType.INTELLECT, 0.5, 5));
            abilities.add(ability("Flash of Light", "A quick burst of healing light.", CharacterClass.PALADIN, "HOLY", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.HOLY_POWER, 15, 0, 15, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Avenger's Shield", "A holy shield thrown at an enemy.", CharacterClass.PALADIN, "PROTECTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.HOLY_POWER, 20, 1, 16, StatType.STRENGTH, 0.35, 4));
            abilities.add(ability("Hammer of the Righteous", "A righteous hammer blow.", CharacterClass.PALADIN, "PROTECTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.HOLY_POWER, 15, 0, 14, StatType.STRENGTH, 0.3, 1));
            abilities.add(ability("Templar's Verdict", "A final judgment delivered with the blade.", CharacterClass.PALADIN, "RETRIBUTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.HOLY_POWER, 25, 0, 20, StatType.STRENGTH, 0.5, 1));
            abilities.add(ability("Judgment", "Marks the target for holy retribution.", CharacterClass.PALADIN, "RETRIBUTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.HOLY_POWER, 15, 1, 14, StatType.STRENGTH, 0.35, 4));
            abilities.add(ability("Heal", "A basic restorative spell.", CharacterClass.PRIEST, "HOLY", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 30, 0, 20, StatType.SPIRIT, 0.45, 5));
            abilities.add(ability("Prayer of Healing", "A prayer that mends every ally.", CharacterClass.PRIEST, "HOLY", AbilityType.HEAL, TargetType.ALL_ALLIES, PowerResourceType.MANA, 45, 1, 14, StatType.SPIRIT, 0.3, 5));
            abilities.add(ability("Penance", "Channels holy energy into the target.", CharacterClass.PRIEST, "DISCIPLINE", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 25, 0, 18, StatType.SPIRIT, 0.4, 5));
            abilities.add(ability("Power Word: Shield", "Shields an ally, absorbing harm as restored vitality.", CharacterClass.PRIEST, "DISCIPLINE", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 20, 1, 16, StatType.SPIRIT, 0.35, 5));
            abilities.add(ability("Mind Blast", "A direct assault on the mind.", CharacterClass.PRIEST, "SHADOW", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 0, 20, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Mind Flay", "Channels shadow energy to wither the target.", CharacterClass.PRIEST, "SHADOW", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 15, 0, 12, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Mutilate", "A vicious dual strike with poisoned blades.", CharacterClass.ROGUE, "ASSASSINATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 30, 0, 18, StatType.AGILITY, 0.45, 1));
            abilities.add(ability("Envenom", "A poison-empowered finishing move.", CharacterClass.ROGUE, "ASSASSINATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 25, 1, 16, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Sinister Strike", "A quick, precise stab.", CharacterClass.ROGUE, "OUTLAW", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 20, 0, 15, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Pistol Shot", "A close-range pistol shot.", CharacterClass.ROGUE, "OUTLAW", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 15, 0, 12, StatType.AGILITY, 0.3, 3));
            abilities.add(ability("Backstab", "A strike from behind for extra damage.", CharacterClass.ROGUE, "SUBTLETY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 25, 0, 18, StatType.AGILITY, 0.45, 1));
            abilities.add(ability("Shadowstrike", "A shadow-cloaked ambush.", CharacterClass.ROGUE, "SUBTLETY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 20, 1, 16, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Lightning Bolt", "Calls down a bolt of lightning.", CharacterClass.SHAMAN, "ELEMENTAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 0, 18, StatType.INTELLECT, 0.4, 5));
            abilities.add(ability("Lava Burst", "Hurls a burst of molten lava.", CharacterClass.SHAMAN, "ELEMENTAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 30, 1, 22, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Stormstrike", "A weapon strike wreathed in storm energy.", CharacterClass.SHAMAN, "ENHANCEMENT", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 16, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Lava Lash", "A flame-wreathed off-hand strike.", CharacterClass.SHAMAN, "ENHANCEMENT", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 15, 0, 13, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Healing Wave", "A rolling wave of restorative energy.", CharacterClass.SHAMAN, "RESTORATION", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 30, 0, 22, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Riptide", "A wave of water that heals over time, applied instantly here.", CharacterClass.SHAMAN, "RESTORATION", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 20, 1, 16, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Kill Command", "Commands your pet to strike the target.", CharacterClass.HUNTER, "BEAST_MASTERY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 30, 0, 18, StatType.AGILITY, 0.4, 6));
            abilities.add(ability("Cobra Shot", "A quick shot that also refreshes focus.", CharacterClass.HUNTER, "BEAST_MASTERY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 20, 0, 13, StatType.AGILITY, 0.3, 6));
            abilities.add(ability("Aimed Shot", "A carefully aimed, powerful shot.", CharacterClass.HUNTER, "MARKSMANSHIP", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 35, 1, 24, StatType.AGILITY, 0.5, 7));
            abilities.add(ability("Rapid Fire", "A rapid volley of arrows.", CharacterClass.HUNTER, "MARKSMANSHIP", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 25, 1, 16, StatType.AGILITY, 0.35, 6));
            abilities.add(ability("Raptor Strike", "A swift melee strike.", CharacterClass.HUNTER, "SURVIVAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 20, 0, 15, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Wildfire Bomb", "An explosive thrown at the target.", CharacterClass.HUNTER, "SURVIVAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FOCUS, 30, 1, 20, StatType.AGILITY, 0.4, 4));
            abilities.add(ability("Death Strike", "A vicious strike that drains life back to the caster.", CharacterClass.DEATH_KNIGHT, "BLOOD", AbilityType.HEAL, TargetType.SELF, PowerResourceType.RUNIC_POWER, 25, 0, 15, StatType.STRENGTH, 0.35, 0));
            abilities.add(ability("Blood Boil", "Boils the blood of nearby enemies.", CharacterClass.DEATH_KNIGHT, "BLOOD", AbilityType.DAMAGE, TargetType.ALL_ENEMIES, PowerResourceType.RUNIC_POWER, 20, 1, 12, StatType.STRENGTH, 0.3, 1));
            abilities.add(ability("Obliterate", "A devastating frost-infused strike.", CharacterClass.DEATH_KNIGHT, "FROST", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RUNIC_POWER, 30, 0, 22, StatType.STRENGTH, 0.5, 1));
            abilities.add(ability("Frost Strike", "A chilling finishing strike.", CharacterClass.DEATH_KNIGHT, "FROST", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RUNIC_POWER, 20, 1, 16, StatType.STRENGTH, 0.35, 1));
            abilities.add(ability("Festering Strike", "A diseased strike that festers the wound.", CharacterClass.DEATH_KNIGHT, "UNHOLY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RUNIC_POWER, 20, 0, 15, StatType.STRENGTH, 0.35, 1));
            abilities.add(ability("Scourge Strike", "A strike empowered by shadow magic.", CharacterClass.DEATH_KNIGHT, "UNHOLY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.RUNIC_POWER, 25, 1, 18, StatType.STRENGTH, 0.4, 1));
            abilities.add(ability("Arcane Blast", "A blast of raw arcane energy.", CharacterClass.MAGE, "ARCANE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 35, 0, 24, StatType.INTELLECT, 0.5, 5));
            abilities.add(ability("Arcane Barrage", "A barrage of arcane missiles.", CharacterClass.MAGE, "ARCANE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 1, 16, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Fireball", "A classic bolt of fire.", CharacterClass.MAGE, "FIRE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 0, 18, StatType.INTELLECT, 0.4, 5));
            abilities.add(ability("Pyroblast", "A massive, slow-building fireball.", CharacterClass.MAGE, "FIRE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 40, 2, 30, StatType.INTELLECT, 0.55, 5));
            abilities.add(ability("Frostbolt", "A bolt of freezing ice.", CharacterClass.MAGE, "FROST", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 0, 17, StatType.INTELLECT, 0.4, 5));
            abilities.add(ability("Ice Lance", "A fast lance of ice.", CharacterClass.MAGE, "FROST", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 15, 0, 12, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Shadow Bolt", "A bolt of raw shadow energy.", CharacterClass.WARLOCK, "AFFLICTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 0, 17, StatType.INTELLECT, 0.4, 5));
            abilities.add(ability("Agony", "A curse that wracks the target with pain.", CharacterClass.WARLOCK, "AFFLICTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 13, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Hand of Gul'dan", "Summons a rain of fel fire.", CharacterClass.WARLOCK, "DEMONOLOGY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 30, 1, 20, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Demonbolt", "A bolt of demonic energy.", CharacterClass.WARLOCK, "DEMONOLOGY", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 14, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Chaos Bolt", "An unstoppable bolt of chaotic fire.", CharacterClass.WARLOCK, "DESTRUCTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 35, 1, 26, StatType.INTELLECT, 0.5, 5));
            abilities.add(ability("Incinerate", "A bolt of scorching fire.", CharacterClass.WARLOCK, "DESTRUCTION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 14, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Keg Smash", "Smashes a keg into the enemy, dealing damage.", CharacterClass.MONK, "BREWMASTER", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 25, 1, 16, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Blackout Kick", "A powerful spinning kick.", CharacterClass.MONK, "BREWMASTER", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 15, 0, 12, StatType.AGILITY, 0.3, 1));
            abilities.add(ability("Soothing Mist", "A calming mist that mends wounds.", CharacterClass.MONK, "MISTWEAVER", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.ENERGY, 25, 0, 18, StatType.SPIRIT, 0.4, 5));
            abilities.add(ability("Enveloping Mist", "Envelops an ally in restorative mist.", CharacterClass.MONK, "MISTWEAVER", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.ENERGY, 30, 1, 22, StatType.SPIRIT, 0.45, 5));
            abilities.add(ability("Tiger Palm", "A quick, precise palm strike.", CharacterClass.MONK, "WINDWALKER", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 15, 0, 12, StatType.AGILITY, 0.3, 1));
            abilities.add(ability("Rising Sun Kick", "A powerful rising kick.", CharacterClass.MONK, "WINDWALKER", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ENERGY, 25, 1, 18, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Wrath", "A bolt of nature's fury.", CharacterClass.DRUID, "BALANCE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 15, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Starsurge", "A surge of astral energy.", CharacterClass.DRUID, "BALANCE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 30, 1, 22, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Rake", "Rakes the target with claws.", CharacterClass.DRUID, "FERAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 14, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Ferocious Bite", "A vicious finishing bite.", CharacterClass.DRUID, "FERAL", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 25, 1, 18, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Mangle", "A mauling strike that tears at armor.", CharacterClass.DRUID, "GUARDIAN", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.MANA, 20, 0, 15, StatType.AGILITY, 0.35, 1));
            abilities.add(ability("Thrash", "A wide, thrashing swipe.", CharacterClass.DRUID, "GUARDIAN", AbilityType.DAMAGE, TargetType.ALL_ENEMIES, PowerResourceType.MANA, 25, 1, 12, StatType.AGILITY, 0.3, 1));
            abilities.add(ability("Rejuvenation", "A gentle restorative surge.", CharacterClass.DRUID, "RESTORATION", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 20, 0, 16, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Regrowth", "A fast, potent healing spell.", CharacterClass.DRUID, "RESTORATION", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.MANA, 30, 1, 22, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Chaos Strike", "A furious demonic strike.", CharacterClass.DEMON_HUNTER, "HAVOC", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FURY, 25, 0, 18, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Blade Dance", "A whirling dance of blades.", CharacterClass.DEMON_HUNTER, "HAVOC", AbilityType.DAMAGE, TargetType.ALL_ENEMIES, PowerResourceType.FURY, 30, 1, 14, StatType.AGILITY, 0.3, 1));
            abilities.add(ability("Shear", "A demonic blade strike.", CharacterClass.DEMON_HUNTER, "VENGEANCE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FURY, 15, 0, 13, StatType.AGILITY, 0.3, 1));
            abilities.add(ability("Soul Cleave", "A cleaving strike fueled by consumed souls.", CharacterClass.DEMON_HUNTER, "VENGEANCE", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.FURY, 25, 1, 18, StatType.AGILITY, 0.4, 1));
            abilities.add(ability("Living Flame", "Hurls a bolt of draconic fire.", CharacterClass.EVOKER, "DEVASTATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ESSENCE, 20, 0, 16, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Disintegrate", "A disintegrating beam of energy.", CharacterClass.EVOKER, "DEVASTATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ESSENCE, 30, 1, 22, StatType.INTELLECT, 0.45, 5));
            abilities.add(ability("Living Flame", "A warm draconic flame that mends wounds.", CharacterClass.EVOKER, "PRESERVATION", AbilityType.HEAL, TargetType.SINGLE_ALLY, PowerResourceType.ESSENCE, 20, 0, 16, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Dream Breath", "A breath of restorative dream energy.", CharacterClass.EVOKER, "PRESERVATION", AbilityType.HEAL, TargetType.ALL_ALLIES, PowerResourceType.ESSENCE, 35, 1, 14, StatType.INTELLECT, 0.3, 5));
            abilities.add(ability("Living Flame", "Hurls a bolt of draconic fire.", CharacterClass.EVOKER, "AUGMENTATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ESSENCE, 20, 0, 15, StatType.INTELLECT, 0.35, 5));
            abilities.add(ability("Upheaval", "Erupts the ground beneath the target.", CharacterClass.EVOKER, "AUGMENTATION", AbilityType.DAMAGE, TargetType.SINGLE_ENEMY, PowerResourceType.ESSENCE, 25, 1, 18, StatType.INTELLECT, 0.4, 5));

            Flux.fromIterable(abilities)
                .flatMap(abilityRepository::save)
                .blockLast();

            System.out.println("Abilities loaded: " + abilities.size());
        };
    }

    private Ability ability(String name, String description, CharacterClass characterClass, String specialization,
                             AbilityType abilityType, TargetType targetType, PowerResourceType resourceType,
                             int resourceCost, int cooldownTurns, int basePower, StatType scalingStat,
                             double scalingFactor, int range) {
        return Ability.builder()
                .name(name)
                .description(description)
                .characterClass(characterClass)
                .specialization(specialization)
                .abilityType(abilityType)
                .targetType(targetType)
                .resourceType(resourceType)
                .resourceCost(resourceCost)
                .cooldownTurns(cooldownTurns)
                .basePower(basePower)
                .scalingStat(scalingStat)
                .scalingFactor(scalingFactor)
                .range(range)
                .build();
    }
}
