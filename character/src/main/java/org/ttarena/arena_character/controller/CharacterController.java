package org.ttarena.arena_character.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.Paladin;
import org.ttarena.arena_character.model.Rogue;
import org.ttarena.arena_character.model.Shaman;
import org.ttarena.arena_character.model.Hunter;
import org.ttarena.arena_character.model.DeathKnight;
import org.ttarena.arena_character.model.Mage;
import org.ttarena.arena_character.model.Warlock;
import org.ttarena.arena_character.model.Monk;
import org.ttarena.arena_character.model.Druid;
import org.ttarena.arena_character.model.DemonHunter;
import org.ttarena.arena_character.model.Evoker;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.RogueSpecialization;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {
    
    private final CharacterService characterService;
    
    @Autowired
    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getAllCharacters() {
        return characterService.getAllCharacters();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> getCharacterById(@PathVariable String id) {
        return characterService.getCharacterById(id);
    }

    @GetMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> getCharacterByName(@PathVariable String name) {
        return characterService.getCharacterByName(name);
    }

    @GetMapping(value = "/class/{characterClass}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getCharactersByClass(@PathVariable CharacterClass characterClass) {
        return characterService.getCharactersByClass(characterClass);
    }

    @PostMapping(value = "/warrior", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Warrior> createWarrior(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int rage,
            @RequestParam WarriorSpecialization specialization) {
        return characterService.createWarrior(name, health, rage, specialization);
    }

    @PostMapping(value = "/priest", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Priest> createPriest(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam PriestSpecialization specialization) {
        return characterService.createPriest(name, health, mana, specialization);
    }

    @PostMapping(value = "/paladin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paladin> createPaladin(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int holyPower,
            @RequestParam PaladinSpecialization specialization) {
        return characterService.createPaladin(name, health, holyPower, specialization);
    }

    @PostMapping(value = "/rogue", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Rogue> createRogue(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int energy,
            @RequestParam RogueSpecialization specialization) {
        return characterService.createRogue(name, health, energy, specialization);
    }

    @PostMapping(value = "/shaman", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Shaman> createShaman(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam ShamanSpecialization specialization) {
        return characterService.createShaman(name, health, mana, specialization);
    }

    @PostMapping(value = "/hunter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Hunter> createHunter(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int focus,
            @RequestParam HunterSpecialization specialization) {
        return characterService.createHunter(name, health, focus, specialization);
    }

    @PostMapping(value = "/death-knight", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DeathKnight> createDeathKnight(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int runicPower,
            @RequestParam DeathKnightSpecialization specialization) {
        return characterService.createDeathKnight(name, health, runicPower, specialization);
    }

    @PostMapping(value = "/mage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Mage> createMage(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam MageSpecialization specialization) {
        return characterService.createMage(name, health, mana, specialization);
    }

    @PostMapping(value = "/warlock", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Warlock> createWarlock(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam WarlockSpecialization specialization) {
        return characterService.createWarlock(name, health, mana, specialization);
    }

    @PostMapping(value = "/monk", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Monk> createMonk(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int energy,
            @RequestParam MonkSpecialization specialization) {
        return characterService.createMonk(name, health, energy, specialization);
    }

    @PostMapping(value = "/druid", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Druid> createDruid(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam DruidSpecialization specialization) {
        return characterService.createDruid(name, health, mana, specialization);
    }

    @PostMapping(value = "/demon-hunter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DemonHunter> createDemonHunter(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int fury,
            @RequestParam DemonHunterSpecialization specialization) {
        return characterService.createDemonHunter(name, health, fury, specialization);
    }

    @PostMapping(value = "/evoker", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Evoker> createEvoker(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int essence,
            @RequestParam EvokerSpecialization specialization) {
        return characterService.createEvoker(name, health, essence, specialization);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> updateCharacter(
            @PathVariable String id,
            @RequestBody Character character) {
        return characterService.updateCharacter(id, character);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCharacter(@PathVariable String id) {
        return characterService.deleteCharacter(id);
    }
}
