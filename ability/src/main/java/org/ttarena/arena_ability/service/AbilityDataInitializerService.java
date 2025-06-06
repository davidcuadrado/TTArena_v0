package org.ttarena.arena_ability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.ttarena.arena_ability.model.*;
import org.ttarena.arena_ability.repository.AbilityRepository;

import java.util.List;

/**
 * Servicio para inicializar las habilidades de World of Warcraft en la base de datos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityDataInitializerService implements CommandLineRunner {
    
    private final AbilityRepository abilityRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (abilityRepository.count() == 0) {
            log.info("Inicializando habilidades de World of Warcraft...");
            initializePaladinAbilities();
            initializeAdditionalAbilities();
            log.info("Habilidades de World of Warcraft inicializadas correctamente");
        } else {
            log.info("Las habilidades ya están inicializadas en la base de datos");
        }
    }
    
    private void initializePaladinAbilities() {
        List<Ability> paladinAbilities = List.of(
            // Habilidades generales de Paladín
            Ability.builder()
                .name("Judgement")
                .description("Juzga al enemigo, causando daño sagrado y aplicando efectos según el sello activo.")
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
                .description("Un ataque instantáneo que causa daño de arma y genera 1 punto de Poder Sagrado.")
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
                .description("Lanza un martillo sagrado que causa daño a distancia.")
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
                .description("Cura instantánea que consume Poder Sagrado para restaurar salud.")
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
                .description("Curación rápida con tiempo de lanzamiento corto.")
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
                .description("Reduce el daño recibido en un 40% durante 10 segundos.")
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
                .description("Protege al objetivo de todos los ataques físicos durante 10 segundos.")
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
                .description("Transfiere el 30% del daño recibido por el objetivo al Paladín durante 12 segundos.")
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
                
            // Habilidades de Paladín Retribución
            Ability.builder()
                .name("Final Verdict")
                .description("Un poderoso ataque final que consume todo el Poder Sagrado para causar daño masivo.")
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
                .description("Barre el área con cenizas sagradas, causando daño y generando 3 puntos de Poder Sagrado.")
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
                
            // Habilidades de Paladín Sagrado
            Ability.builder()
                .name("Holy Shock")
                .description("Hechizo instantáneo que puede curar a aliados o dañar a enemigos.")
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
                .description("Marca a un aliado como faro, transfiriendo parte de la curación realizada a otros objetivos.")
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
                
            // Habilidades de Paladín Protección
            Ability.builder()
                .name("Avenger's Shield")
                .description("Lanza el escudo que rebota entre múltiples enemigos, causando daño y silenciándolos.")
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
                .description("Reduce el daño recibido en un 20% y previene la muerte fatal durante 8 segundos.")
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
        );
        
        abilityRepository.saveAll(paladinAbilities);
        log.info("Guardadas {} habilidades de Paladín", paladinAbilities.size());
    }
}


    private void initializeAdditionalAbilities() {
        List<Ability> additionalAbilities = List.of(
            // Habilidades adicionales de Priest
            Ability.builder()
                .name("Mind Control")
                .description("Controla la mente de un enemigo humanoid durante 8 segundos.")
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
                .description("Envuelve al objetivo en un escudo protector que absorbe daño.")
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
                .description("Inflige daño de las Sombras al objetivo durante 18 segundos.")
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
                .description("Canaliza daño de las Sombras y ralentiza al objetivo.")
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
                
            // Habilidades adicionales de Rogue
            Ability.builder()
                .name("Stealth")
                .description("Se vuelve invisible durante 10 segundos o hasta atacar.")
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
                .description("Ataque desde atrás que causa daño aumentado.")
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
                .description("Desaparece instantáneamente, entrando en sigilo mejorado.")
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
                .description("Se teletransporta detrás del objetivo enemigo.")
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
                
            // Habilidades adicionales de Shaman
            Ability.builder()
                .name("Chain Lightning")
                .description("Lanza un rayo que salta entre múltiples enemigos.")
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
                .description("Inflige daño de Escarcha y ralentiza al objetivo.")
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
                .description("Invoca un Elemental de Tierra para tanquear durante 60 segundos.")
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
                .description("Invoca lluvia curativa en un área durante 10 segundos.")
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
                
            // Habilidades adicionales de Warrior
            Ability.builder()
                .name("Mortal Strike")
                .description("Ataque devastador que reduce la curación recibida por el objetivo.")
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
                .description("Serie de ataques furiosos que aumentan el daño.")
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
                .description("Reduce el daño recibido en un 40% durante 8 segundos.")
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
                .description("Aterroriza a los enemigos cercanos, haciéndolos huir.")
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
        );

        abilityRepository.saveAll(additionalAbilities);
        log.info("Guardadas {} habilidades adicionales", additionalAbilities.size());
    }

