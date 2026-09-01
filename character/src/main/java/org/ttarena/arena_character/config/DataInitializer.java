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
import org.ttarena.arena_character.repository.CharacterRepository;
import org.ttarena.arena_character.service.CharacterService;
import reactor.core.publisher.Flux;

@Configuration
@Profile("dev")
public class DataInitializer {
    @Bean
    public CommandLineRunner loadData(CharacterService characterService,
                                      CharacterRepository characterRepository) {
        return args -> {
            characterRepository.deleteAll().block();

            Flux.concat(
                characterService.createCharacter(
                    request("Conan", CharacterClass.WARRIOR, 200, 100, WarriorSpecialization.ARMS),
                    devOwnerFor(CharacterClass.WARRIOR)),
                characterService.createCharacter(
                    request("Garrosh", CharacterClass.WARRIOR, 180, 120, WarriorSpecialization.FURY),
                    devOwnerFor(CharacterClass.WARRIOR)),
                characterService.createCharacter(
                    request("Muradin", CharacterClass.WARRIOR, 250, 80, WarriorSpecialization.PROTECTION),
                    devOwnerFor(CharacterClass.WARRIOR))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Anduin", CharacterClass.PRIEST, 150, 200, PriestSpecialization.HOLY),
                    devOwnerFor(CharacterClass.PRIEST)),
                characterService.createCharacter(
                    request("Moira", CharacterClass.PRIEST, 140, 180, PriestSpecialization.DISCIPLINE),
                    devOwnerFor(CharacterClass.PRIEST)),
                characterService.createCharacter(
                    request("Velen", CharacterClass.PRIEST, 130, 220, PriestSpecialization.SHADOW),
                    devOwnerFor(CharacterClass.PRIEST))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Uther", CharacterClass.PALADIN, 220, 150, PaladinSpecialization.HOLY),
                    devOwnerFor(CharacterClass.PALADIN)),
                characterService.createCharacter(
                    request("Tirion", CharacterClass.PALADIN, 200, 170, PaladinSpecialization.RETRIBUTION),
                    devOwnerFor(CharacterClass.PALADIN)),
                characterService.createCharacter(
                    request("Bolvar", CharacterClass.PALADIN, 240, 130, PaladinSpecialization.PROTECTION),
                    devOwnerFor(CharacterClass.PALADIN))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Valeera", CharacterClass.ROGUE, 160, 180, RogueSpecialization.ASSASSINATION),
                    devOwnerFor(CharacterClass.ROGUE)),
                characterService.createCharacter(
                    request("Mathias", CharacterClass.ROGUE, 150, 200, RogueSpecialization.SUBTLETY),
                    devOwnerFor(CharacterClass.ROGUE)),
                characterService.createCharacter(
                    request("Flynn", CharacterClass.ROGUE, 170, 160, RogueSpecialization.OUTLAW),
                    devOwnerFor(CharacterClass.ROGUE))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Thrall", CharacterClass.SHAMAN, 190, 170, ShamanSpecialization.ENHANCEMENT),
                    devOwnerFor(CharacterClass.SHAMAN)),
                characterService.createCharacter(
                    request("Nobundo", CharacterClass.SHAMAN, 170, 190, ShamanSpecialization.RESTORATION),
                    devOwnerFor(CharacterClass.SHAMAN)),
                characterService.createCharacter(
                    request("Magatha", CharacterClass.SHAMAN, 160, 210, ShamanSpecialization.ELEMENTAL),
                    devOwnerFor(CharacterClass.SHAMAN))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Rexxar", CharacterClass.HUNTER, 190, 140, HunterSpecialization.BEAST_MASTERY),
                    devOwnerFor(CharacterClass.HUNTER)),
                characterService.createCharacter(
                    request("Alleria", CharacterClass.HUNTER, 150, 160, HunterSpecialization.MARKSMANSHIP),
                    devOwnerFor(CharacterClass.HUNTER)),
                characterService.createCharacter(
                    request("Vereesa", CharacterClass.HUNTER, 155, 150, HunterSpecialization.SURVIVAL),
                    devOwnerFor(CharacterClass.HUNTER))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Arthas", CharacterClass.DEATH_KNIGHT, 230, 100, DeathKnightSpecialization.UNHOLY),
                    devOwnerFor(CharacterClass.DEATH_KNIGHT)),
                characterService.createCharacter(
                    request("Darion", CharacterClass.DEATH_KNIGHT, 220, 110, DeathKnightSpecialization.BLOOD),
                    devOwnerFor(CharacterClass.DEATH_KNIGHT)),
                characterService.createCharacter(
                    request("Koltira", CharacterClass.DEATH_KNIGHT, 200, 120, DeathKnightSpecialization.FROST),
                    devOwnerFor(CharacterClass.DEATH_KNIGHT))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Jaina", CharacterClass.MAGE, 140, 230, MageSpecialization.FROST),
                    devOwnerFor(CharacterClass.MAGE)),
                characterService.createCharacter(
                    request("Khadgar", CharacterClass.MAGE, 130, 240, MageSpecialization.ARCANE),
                    devOwnerFor(CharacterClass.MAGE)),
                characterService.createCharacter(
                    request("Kalecgos", CharacterClass.MAGE, 135, 220, MageSpecialization.FIRE),
                    devOwnerFor(CharacterClass.MAGE))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Guldan", CharacterClass.WARLOCK, 150, 210, WarlockSpecialization.DEMONOLOGY),
                    devOwnerFor(CharacterClass.WARLOCK)),
                characterService.createCharacter(
                    request("Teron", CharacterClass.WARLOCK, 140, 200, WarlockSpecialization.AFFLICTION),
                    devOwnerFor(CharacterClass.WARLOCK)),
                characterService.createCharacter(
                    request("Kanrethad", CharacterClass.WARLOCK, 145, 215, WarlockSpecialization.DESTRUCTION),
                    devOwnerFor(CharacterClass.WARLOCK))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Chen", CharacterClass.MONK, 190, 170, MonkSpecialization.BREWMASTER),
                    devOwnerFor(CharacterClass.MONK)),
                characterService.createCharacter(
                    request("Taran", CharacterClass.MONK, 160, 190, MonkSpecialization.MISTWEAVER),
                    devOwnerFor(CharacterClass.MONK)),
                characterService.createCharacter(
                    request("Lili", CharacterClass.MONK, 165, 180, MonkSpecialization.WINDWALKER),
                    devOwnerFor(CharacterClass.MONK))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Malfurion", CharacterClass.DRUID, 200, 190, DruidSpecialization.BALANCE),
                    devOwnerFor(CharacterClass.DRUID)),
                characterService.createCharacter(
                    request("Broll", CharacterClass.DRUID, 210, 160, DruidSpecialization.FERAL),
                    devOwnerFor(CharacterClass.DRUID)),
                characterService.createCharacter(
                    request("Naralex", CharacterClass.DRUID, 220, 150, DruidSpecialization.GUARDIAN),
                    devOwnerFor(CharacterClass.DRUID))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Illidan", CharacterClass.DEMON_HUNTER, 200, 100, DemonHunterSpecialization.HAVOC),
                    devOwnerFor(CharacterClass.DEMON_HUNTER)),
                characterService.createCharacter(
                    request("Kayn", CharacterClass.DEMON_HUNTER, 190, 110, DemonHunterSpecialization.HAVOC),
                    devOwnerFor(CharacterClass.DEMON_HUNTER)),
                characterService.createCharacter(
                    request("Altruis", CharacterClass.DEMON_HUNTER, 210, 120, DemonHunterSpecialization.VENGEANCE),
                    devOwnerFor(CharacterClass.DEMON_HUNTER))
            ).blockLast();

            Flux.concat(
                characterService.createCharacter(
                    request("Alexstrasza", CharacterClass.EVOKER, 190, 180, EvokerSpecialization.PRESERVATION),
                    devOwnerFor(CharacterClass.EVOKER)),
                characterService.createCharacter(
                    request("Nozdormu", CharacterClass.EVOKER, 170, 200, EvokerSpecialization.AUGMENTATION),
                    devOwnerFor(CharacterClass.EVOKER)),
                characterService.createCharacter(
                    request("Sabellian", CharacterClass.EVOKER, 180, 190, EvokerSpecialization.DEVASTATION),
                    devOwnerFor(CharacterClass.EVOKER))
            ).blockLast();

            System.out.println("Personajes cargados:");
            characterService.getAllCharacters()
                .doOnNext(System.out::println)
                .blockLast();
        };
    }

    private static String devOwnerFor(CharacterClass characterClass) {
        return "dev-" + characterClass.name().toLowerCase();
    }

    private static CreateCharacterRequest request(String name, CharacterClass characterClass,
                                                  int health, int resourceAmount, Enum<?> specialization) {
        return new CreateCharacterRequest(name, characterClass, health, resourceAmount, specialization.name());
    }
}
