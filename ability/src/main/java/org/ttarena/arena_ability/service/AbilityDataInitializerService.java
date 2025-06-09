package org.ttarena.arena_ability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.ttarena.arena_ability.model.*;
import org.ttarena.arena_ability.repository.AbilityRepository;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityDataInitializerService implements CommandLineRunner {
    
    private final AbilityRepository abilityRepository;
    
    @Override
    public void run(String... args) throws Exception {
        abilityRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        log.info("Initializing abilities...");
                        return initializeAllAbilities()
                                .doOnComplete(() -> log.info("Abilities initialized successfully"));
                    } else {
                        log.info("Abilities are already initialized in the database (count: {})", count);
                        return Flux.empty();
                    }
                })
                .subscribe();
    }
    
    private Flux<Ability> initializeAllAbilities() {
        return Flux.concat(
                initializePaladinAbilities(),
                initializeAdditionalAbilities()
        );
    }
    
    private Flux<Ability> initializePaladinAbilities() {
        Flux<Ability> paladinAbilities = Flux.fromIterable(java.util.List.of(
            // General Paladin abilities
            Ability.builder()
                .name("Judgement")
                .description("Judges the enemy, causing holy damage and applying effects based on the active seal.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_righteousfury.jpg")
                .cooldown(30)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.OFFENSIVE)
                .wowClass(WowClass.PALADIN)
                .baseValue(150)
                .range(20)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Crusader Strike")
                .description("An instant attack that causes weapon damage and generates 1 Holy Power.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_crusaderstrike.jpg")
                .cooldown(6)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.OFFENSIVE)
                .wowClass(WowClass.PALADIN)
                .baseValue(100)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Hammer of Wrath")
                .description("Throws a holy hammer that causes ranged damage.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_thunderbolt.jpg")
                .cooldown(30)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.OFFENSIVE)
                .wowClass(WowClass.PALADIN)
                .baseValue(120)
                .range(30)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Word of Glory")
                .description("Instant heal that consumes Holy Power to restore health.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/inv_helmet_96.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(3)
                .resourceType(ResourceType.HOLY_POWER)
                .abilityType(AbilityType.HEALING)
                .wowClass(WowClass.PALADIN)
                .baseValue(200)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Flash of Light")
                .description("Fast healing with short cast time.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_flashheal.jpg")
                .cooldown(0)
                .castTime(1500)
                .resourceCost(2200)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.HEALING)
                .wowClass(WowClass.PALADIN)
                .baseValue(180)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Divine Protection")
                .description("Reduces damage taken by 40% for 10 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_restoration.jpg")
                .cooldown(300)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.DEFENSIVE)
                .wowClass(WowClass.PALADIN)
                .baseValue(40)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Blessing of Protection")
                .description("Protects the target from all physical attacks for 10 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_sealofprotection.jpg")
                .cooldown(300)
                .castTime(0)
                .resourceCost(1500)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.BUFF)
                .wowClass(WowClass.PALADIN)
                .baseValue(0)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Blessing of Sacrifice")
                .description("Transfers 30% of damage taken by the target to the Paladin for 12 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_sealofsacrifice.jpg")
                .cooldown(120)
                .castTime(0)
                .resourceCost(1000)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.BUFF)
                .wowClass(WowClass.PALADIN)
                .baseValue(30)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            // Retribution Paladin abilities
            Ability.builder()
                .name("Final Verdict")
                .description("A powerful finishing attack that consumes all Holy Power to deal massive damage.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_paladin_hammerofwrath.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(3)
                .resourceType(ResourceType.HOLY_POWER)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.RETRIBUTION_PALADIN)
                .baseValue(300)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Wake of Ashes")
                .description("Sweeps the area with holy ashes, dealing damage and generating 3 Holy Power.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/inv_sword_2h_artifactashbringer_d_03.jpg")
                .cooldown(45)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.RETRIBUTION_PALADIN)
                .baseValue(250)
                .range(0)
                .areaEffect(true)
                .areaRadius(8)
                .build(),
                
            // Holy Paladin abilities
            Ability.builder()
                .name("Holy Shock")
                .description("Instant spell that can heal allies or damage enemies.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_searinglight.jpg")
                .cooldown(7)
                .castTime(0)
                .resourceCost(1600)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.HEALING)
                .specialization(Specialization.HOLY_PALADIN)
                .baseValue(220)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Beacon of Light")
                .description("Marks an ally as a beacon, transferring part of healing done to other targets.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_paladin_beaconoflight.jpg")
                .cooldown(0)
                .castTime(1500)
                .resourceCost(2500)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.BUFF)
                .specialization(Specialization.HOLY_PALADIN)
                .baseValue(50)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            // Protection Paladin abilities
            Ability.builder()
                .name("Avenger's Shield")
                .description("Throws the shield that bounces between multiple enemies, dealing damage and silencing them.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_avengersshield.jpg")
                .cooldown(15)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.PROTECTION_PALADIN)
                .baseValue(140)
                .range(30)
                .areaEffect(true)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Ardent Defender")
                .description("Reduces damage taken by 20% and prevents fatal death for 8 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_ardentdefender.jpg")
                .cooldown(120)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.DEFENSIVE)
                .specialization(Specialization.PROTECTION_PALADIN)
                .baseValue(20)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build()
        ));
        
        return abilityRepository.saveAll(paladinAbilities)
                .doOnNext(ability -> log.debug("Saved Paladin ability: {}", ability.getName()))
                .doOnComplete(() -> log.info("Saved all Paladin abilities"));
    }
    
    private Flux<Ability> initializeAdditionalAbilities() {
        Flux<Ability> additionalAbilities = Flux.fromIterable(java.util.List.of(
            // Additional Priest abilities
            Ability.builder()
                .name("Mind Control")
                .description("Controls the mind of a humanoid enemy for 8 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_shadow_shadowworddominate.jpg")
                .cooldown(120)
                .castTime(3000)
                .resourceCost(2000)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.DEBUFF)
                .wowClass(WowClass.PRIEST)
                .baseValue(0)
                .range(30)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Power Word: Shield")
                .description("Wraps the target in a protective shield that absorbs damage.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_holy_powerwordshield.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(1800)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.BUFF)
                .wowClass(WowClass.PRIEST)
                .baseValue(300)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Shadow Word: Pain")
                .description("Inflicts Shadow damage to the target over 18 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_shadow_shadowwordpain.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(1200)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.DEBUFF)
                .wowClass(WowClass.PRIEST)
                .baseValue(80)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Mind Flay")
                .description("Channels Shadow damage and slows the target.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_shadow_siphonmana.jpg")
                .cooldown(0)
                .castTime(3000)
                .resourceCost(1000)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.SHADOW_PRIEST)
                .baseValue(150)
                .range(40)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            // Additional Rogue abilities
            Ability.builder()
                .name("Stealth")
                .description("Becomes invisible for 10 seconds or until attacking.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_stealth.jpg")
                .cooldown(2)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.UTILITY)
                .wowClass(WowClass.ROGUE)
                .baseValue(0)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Backstab")
                .description("Attack from behind that causes increased damage.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_backstab.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(40)
                .resourceType(ResourceType.ENERGY)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.SUBTLETY_ROGUE)
                .baseValue(180)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Vanish")
                .description("Disappears instantly, entering improved stealth.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_vanish.jpg")
                .cooldown(90)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.UTILITY)
                .wowClass(WowClass.ROGUE)
                .baseValue(0)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Shadowstep")
                .description("Teleports behind the enemy target.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_rogue_shadowstep.jpg")
                .cooldown(30)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.UTILITY)
                .specialization(Specialization.SUBTLETY_ROGUE)
                .baseValue(0)
                .range(25)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            // Additional Shaman abilities
            Ability.builder()
                .name("Chain Lightning")
                .description("Casts lightning that jumps between multiple enemies.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_nature_chainlightning.jpg")
                .cooldown(0)
                .castTime(2000)
                .resourceCost(1500)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.OFFENSIVE)
                .wowClass(WowClass.SHAMAN)
                .baseValue(200)
                .range(40)
                .areaEffect(true)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Frost Shock")
                .description("Inflicts Frost damage and slows the target.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_frost_frostshock.jpg")
                .cooldown(6)
                .castTime(0)
                .resourceCost(800)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.OFFENSIVE)
                .wowClass(WowClass.SHAMAN)
                .baseValue(120)
                .range(25)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Earth Elemental")
                .description("Summons an Earth Elemental to tank for 60 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_nature_earthelemental_totem.jpg")
                .cooldown(300)
                .castTime(0)
                .resourceCost(2500)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.UTILITY)
                .wowClass(WowClass.SHAMAN)
                .baseValue(0)
                .range(30)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Healing Rain")
                .description("Calls down healing rain in an area for 10 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/spell_nature_giftofthewaterspirit.jpg")
                .cooldown(10)
                .castTime(2000)
                .resourceCost(3000)
                .resourceType(ResourceType.MANA)
                .abilityType(AbilityType.HEALING)
                .specialization(Specialization.RESTORATION_SHAMAN)
                .baseValue(150)
                .range(40)
                .areaEffect(true)
                .areaRadius(10)
                .build(),
                
            // Additional Warrior abilities
            Ability.builder()
                .name("Mortal Strike")
                .description("Devastating attack that reduces healing received by the target.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_warrior_savageblow.jpg")
                .cooldown(6)
                .castTime(0)
                .resourceCost(30)
                .resourceType(ResourceType.RAGE)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.ARMS_WARRIOR)
                .baseValue(250)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Rampage")
                .description("Series of furious attacks that increase damage.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_warrior_rampage.jpg")
                .cooldown(0)
                .castTime(0)
                .resourceCost(85)
                .resourceType(ResourceType.RAGE)
                .abilityType(AbilityType.OFFENSIVE)
                .specialization(Specialization.FURY_WARRIOR)
                .baseValue(300)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Shield Wall")
                .description("Reduces damage taken by 40% for 8 seconds.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_warrior_shieldwall.jpg")
                .cooldown(240)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.DEFENSIVE)
                .specialization(Specialization.PROTECTION_WARRIOR)
                .baseValue(40)
                .range(0)
                .areaEffect(false)
                .areaRadius(0)
                .build(),
                
            Ability.builder()
                .name("Intimidating Shout")
                .description("Terrorizes nearby enemies, causing them to flee.")
                .iconUrl("https://wow.zamimg.com/images/wow/icons/large/ability_golemthunderclap.jpg")
                .cooldown(90)
                .castTime(0)
                .resourceCost(0)
                .resourceType(ResourceType.NONE)
                .abilityType(AbilityType.DEBUFF)
                .wowClass(WowClass.WARRIOR)
                .baseValue(0)
                .range(0)
                .areaEffect(true)
                .areaRadius(8)
                .build()
        ));
        
        return abilityRepository.saveAll(additionalAbilities)
                .doOnNext(ability -> log.debug("Saved additional ability: {}", ability.getName()))
                .doOnComplete(() -> log.info("Saved all additional abilities"));
    }
}

