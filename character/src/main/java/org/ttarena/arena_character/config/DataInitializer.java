package org.ttarena.arena_character.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.RogueSpecialization;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.model.enums.HunterSpecialization;
import org.ttarena.arena_character.model.enums.DeathKnightSpecialization;
import org.ttarena.arena_character.model.enums.MageSpecialization;
import org.ttarena.arena_character.model.enums.WarlockSpecialization;
import org.ttarena.arena_character.model.enums.MonkSpecialization;
import org.ttarena.arena_character.model.enums.DruidSpecialization;
import org.ttarena.arena_character.model.enums.DemonHunterSpecialization;
import org.ttarena.arena_character.model.enums.EvokerSpecialization;
import org.ttarena.arena_character.service.CharacterService;
import reactor.core.publisher.Flux;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(CharacterService characterService) {
        return args -> {
            characterService.getAllCharacters()
                .flatMap(character -> characterService.deleteCharacter(character.getId()))
                .blockLast();

            Flux.just(
                characterService.createWarrior("Conan", 200, 100, WarriorSpecialization.ARMS),
                characterService.createWarrior("Garrosh", 180, 120, WarriorSpecialization.FURY),
                characterService.createWarrior("Muradin", 250, 80, WarriorSpecialization.PROTECTION)
            ).blockLast();

            Flux.just(
                characterService.createPriest("Anduin", 150, 200, PriestSpecialization.HOLY),
                characterService.createPriest("Moira", 140, 180, PriestSpecialization.DISCIPLINE),
                characterService.createPriest("Velen", 130, 220, PriestSpecialization.SHADOW)
            ).blockLast();

            Flux.just(
                characterService.createPaladin("Uther", 220, 150, PaladinSpecialization.HOLY),
                characterService.createPaladin("Tirion", 200, 170, PaladinSpecialization.RETRIBUTION),
                characterService.createPaladin("Bolvar", 240, 130, PaladinSpecialization.PROTECTION)
            ).blockLast();
            
            Flux.just(
                characterService.createRogue("Valeera", 160, 180, RogueSpecialization.ASSASSINATION),
                characterService.createRogue("Mathias", 150, 200, RogueSpecialization.SUBTLETY),
                characterService.createRogue("Flynn", 170, 160, RogueSpecialization.OUTLAW)
            ).blockLast();
            
            Flux.just(
                characterService.createShaman("Thrall", 190, 170, ShamanSpecialization.ENHANCEMENT),
                characterService.createShaman("Nobundo", 170, 190, ShamanSpecialization.RESTORATION),
                characterService.createShaman("Magatha", 160, 210, ShamanSpecialization.ELEMENTAL)
            ).blockLast();

            Flux.just(
                characterService.createHunter("Rexxar", 190, 140, HunterSpecialization.BEAST_MASTERY),
                characterService.createHunter("Alleria", 150, 160, HunterSpecialization.MARKSMANSHIP),
                characterService.createHunter("Vereesa", 155, 150, HunterSpecialization.SURVIVAL)
            ).blockLast();

            Flux.just(
                characterService.createDeathKnight("Arthas", 230, 100, DeathKnightSpecialization.UNHOLY),
                characterService.createDeathKnight("Darion", 220, 110, DeathKnightSpecialization.BLOOD),
                characterService.createDeathKnight("Koltira", 200, 120, DeathKnightSpecialization.FROST)
            ).blockLast();

            Flux.just(
                characterService.createMage("Jaina", 140, 230, MageSpecialization.FROST),
                characterService.createMage("Khadgar", 130, 240, MageSpecialization.ARCANE),
                characterService.createMage("Kalecgos", 135, 220, MageSpecialization.FIRE)
            ).blockLast();

            Flux.just(
                characterService.createWarlock("Guldan", 150, 210, WarlockSpecialization.DEMONOLOGY),
                characterService.createWarlock("Teron", 140, 200, WarlockSpecialization.AFFLICTION),
                characterService.createWarlock("Kanrethad", 145, 215, WarlockSpecialization.DESTRUCTION)
            ).blockLast();

            Flux.just(
                characterService.createMonk("Chen", 190, 170, MonkSpecialization.BREWMASTER),
                characterService.createMonk("Taran", 160, 190, MonkSpecialization.MISTWEAVER),
                characterService.createMonk("Lili", 165, 180, MonkSpecialization.WINDWALKER)
            ).blockLast();

            Flux.just(
                characterService.createDruid("Malfurion", 200, 190, DruidSpecialization.BALANCE),
                characterService.createDruid("Broll", 210, 160, DruidSpecialization.FERAL),
                characterService.createDruid("Naralex", 220, 150, DruidSpecialization.GUARDIAN)
            ).blockLast();

            Flux.just(
                characterService.createDemonHunter("Illidan", 200, 100, DemonHunterSpecialization.HAVOC),
                characterService.createDemonHunter("Kayn", 190, 110, DemonHunterSpecialization.HAVOC),
                characterService.createDemonHunter("Altruis", 210, 120, DemonHunterSpecialization.VENGEANCE)
            ).blockLast();

            Flux.just(
                characterService.createEvoker("Alexstrasza", 190, 180, EvokerSpecialization.PRESERVATION),
                characterService.createEvoker("Nozdormu", 170, 200, EvokerSpecialization.AUGMENTATION),
                characterService.createEvoker("Sabellian", 180, 190, EvokerSpecialization.DEVASTATION)
            ).blockLast();

            System.out.println("Personajes cargados:");
            characterService.getAllCharacters()
                .doOnNext(System.out::println)
                .blockLast();
        };
    }
}
