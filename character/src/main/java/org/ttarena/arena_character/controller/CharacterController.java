package org.ttarena.arena_character.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.Paladin;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
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
