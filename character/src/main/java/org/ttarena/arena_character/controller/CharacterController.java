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
import org.ttarena.arena_character.dto.UpdateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.security.CurrentUser;
import org.ttarena.arena_character.security.CurrentUserProvider;
import org.ttarena.arena_character.service.CharacterService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final CurrentUserProvider currentUserProvider;

    public CharacterController(CharacterService characterService, CurrentUserProvider currentUserProvider) {
        this.characterService = characterService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getAllCharacters() {
        return characterService.getAllCharacters();
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getMyRoster() {
        return currentUserProvider.currentUser()
                .map(CurrentUser::userId)
                .flatMapMany(characterService::getRoster);
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Character> createCharacter(@Valid @RequestBody CreateCharacterRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> characterService.createCharacter(request, currentUser.userId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> updateCharacter(@PathVariable String id,
                                           @Valid @RequestBody UpdateCharacterRequest request) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> characterService.updateCharacter(id, currentUser.userId(), request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCharacter(@PathVariable String id) {
        return currentUserProvider.currentUser()
                .flatMap(currentUser -> characterService.deleteCharacter(id, currentUser.userId()));
    }
}
