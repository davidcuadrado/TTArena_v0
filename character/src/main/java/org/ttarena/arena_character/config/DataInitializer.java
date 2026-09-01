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
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.enums.CharacterClass;
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

            Flux.concat(
                characterService.createCharacter(
                    request("Conan", CharacterClass.WARRIOR, 200, 100, WarriorSpecialization.ARMS)),
                characterService.createCharacter(
                    request("Garrosh", CharacterClass.WARRIOR, 180, 120, WarriorSpecialization.FURY)),
                characterService.createCharacter(
                    request("Muradin", CharacterClass.WARRIOR, 250, 80, WarriorSpecialization.PROTECTION))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Anduin", CharacterClass.PRIEST, 150, 200, PriestSpecialization.HOLY)),
                characterService.createCharacter(
                    request("Moira", CharacterClass.PRIEST, 140, 180, PriestSpecialization.DISCIPLINE)),
                characterService.createCharacter(
                    request("Velen", CharacterClass.PRIEST, 130, 220, PriestSpecialization.SHADOW))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Uther", CharacterClass.PALADIN, 220, 150, PaladinSpecialization.HOLY)),
                characterService.createCharacter(
                    request("Tirion", CharacterClass.PALADIN, 200, 170, PaladinSpecialization.RETRIBUTION)),
                characterService.createCharacter(
                    request("Bolvar", CharacterClass.PALADIN, 240, 130, PaladinSpecialization.PROTECTION))
            ).blockLast();
            
            Flux.concat(
                characterService.createCharacter(
                    request("Valeera", CharacterClass.ROGUE, 160, 180, RogueSpecialization.ASSASSINATION)),
                characterService.createCharacter(
                    request("Mathias", CharacterClass.ROGUE, 150, 200, RogueSpecialization.SUBTLETY)),
                characterService.createCharacter(
                    request("Flynn", CharacterClass.ROGUE, 170, 160, RogueSpecialization.OUTLAW))
            ).blockLast();
            
            Flux.concat(
                characterService.createCharacter(
                    request("Thrall", CharacterClass.SHAMAN, 190, 170, ShamanSpecialization.ENHANCEMENT)),
                characterService.createCharacter(
                    request("Nobundo", CharacterClass.SHAMAN, 170, 190, ShamanSpecialization.RESTORATION)),
                characterService.createCharacter(
                    request("Magatha", CharacterClass.SHAMAN, 160, 210, ShamanSpecialization.ELEMENTAL))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Rexxar", CharacterClass.HUNTER, 190, 140, HunterSpecialization.BEAST_MASTERY)),
                characterService.createCharacter(
                    request("Alleria", CharacterClass.HUNTER, 150, 160, HunterSpecialization.MARKSMANSHIP)),
                characterService.createCharacter(
                    request("Vereesa", CharacterClass.HUNTER, 155, 150, HunterSpecialization.SURVIVAL))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Arthas", CharacterClass.DEATH_KNIGHT, 230, 100, DeathKnightSpecialization.UNHOLY)),
                characterService.createCharacter(
                    request("Darion", CharacterClass.DEATH_KNIGHT, 220, 110, DeathKnightSpecialization.BLOOD)),
                characterService.createCharacter(
                    request("Koltira", CharacterClass.DEATH_KNIGHT, 200, 120, DeathKnightSpecialization.FROST))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Jaina", CharacterClass.MAGE, 140, 230, MageSpecialization.FROST)),
                characterService.createCharacter(
                    request("Khadgar", CharacterClass.MAGE, 130, 240, MageSpecialization.ARCANE)),
                characterService.createCharacter(
                    request("Kalecgos", CharacterClass.MAGE, 135, 220, MageSpecialization.FIRE))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Guldan", CharacterClass.WARLOCK, 150, 210, WarlockSpecialization.DEMONOLOGY)),
                characterService.createCharacter(
                    request("Teron", CharacterClass.WARLOCK, 140, 200, WarlockSpecialization.AFFLICTION)),
                characterService.createCharacter(
                    request("Kanrethad", CharacterClass.WARLOCK, 145, 215, WarlockSpecialization.DESTRUCTION))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Chen", CharacterClass.MONK, 190, 170, MonkSpecialization.BREWMASTER)),
                characterService.createCharacter(
                    request("Taran", CharacterClass.MONK, 160, 190, MonkSpecialization.MISTWEAVER)),
                characterService.createCharacter(
                    request("Lili", CharacterClass.MONK, 165, 180, MonkSpecialization.WINDWALKER))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Malfurion", CharacterClass.DRUID, 200, 190, DruidSpecialization.BALANCE)),
                characterService.createCharacter(
                    request("Broll", CharacterClass.DRUID, 210, 160, DruidSpecialization.FERAL)),
                characterService.createCharacter(
                    request("Naralex", CharacterClass.DRUID, 220, 150, DruidSpecialization.GUARDIAN))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Illidan", CharacterClass.DEMON_HUNTER, 200, 100, DemonHunterSpecialization.HAVOC)),
                characterService.createCharacter(
                    request("Kayn", CharacterClass.DEMON_HUNTER, 190, 110, DemonHunterSpecialization.HAVOC)),
                characterService.createCharacter(
                    request("Altruis", CharacterClass.DEMON_HUNTER, 210, 120, DemonHunterSpecialization.VENGEANCE))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Alexstrasza", CharacterClass.EVOKER, 190, 180, EvokerSpecialization.PRESERVATION)),
                characterService.createCharacter(
                    request("Nozdormu", CharacterClass.EVOKER, 170, 200, EvokerSpecialization.AUGMENTATION)),
                characterService.createCharacter(
                    request("Sabellian", CharacterClass.EVOKER, 180, 190, EvokerSpecialization.DEVASTATION))
            ).blockLast();

            System.out.println("Personajes cargados:");
            characterService.getAllCharacters()
                .doOnNext(System.out::println)
                .blockLast();
        };
    }

    /**
     * Small helper so the seed data below still reads with a typed
     * specialization instead of a bare string.
     */
    private static CreateCharacterRequest request(String name, CharacterClass characterClass,
                                                  int health, int resourceAmount, Enum<?> specialization) {
        return new CreateCharacterRequest(name, characterClass, health, resourceAmount, specialization.name());
    }
}
