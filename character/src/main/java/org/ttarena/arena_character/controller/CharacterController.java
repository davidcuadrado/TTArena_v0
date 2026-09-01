package org.ttarena.arena_character.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.service.CharacterService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

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

    /**
     * Creates a character of any class.
     *
     * <p>Replaces the previous per-class endpoints (/warrior, /priest, ...):
     * the class is now a field in the body, and the matching factory decides
     * which concrete type to build.
     *
     * <pre>
     * POST /api/characters
     * {
     *   "name": "Conan",
     *   "characterClass": "WARRIOR",
     *   "health": 200,
     *   "resourceAmount": 100,
     *   "specialization": "ARMS"
     * }
     * </pre>
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Character> createCharacter(@Valid @RequestBody CreateCharacterRequest request) {
        return characterService.createCharacter(request);
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
