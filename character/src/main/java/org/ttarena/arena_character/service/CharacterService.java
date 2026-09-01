package org.ttarena.arena_character.service;

import org.springframework.stereotype.Service;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.NotFoundException;
import org.ttarena.arena_character.factory.CharacterFactoryRegistry;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final CharacterFactoryRegistry characterFactories;
    private final RosterPolicy rosterPolicy;

    public CharacterService(CharacterRepository characterRepository,
                            CharacterFactoryRegistry characterFactories,
                            RosterPolicy rosterPolicy) {
        this.characterRepository = characterRepository;
        this.characterFactories = characterFactories;
        this.rosterPolicy = rosterPolicy;
    }

    public Flux<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    public Flux<Character> getRoster(String ownerId) {
        return characterRepository.findByOwnerId(ownerId);
    }

    public Mono<Character> getCharacterById(String id) {
        return characterRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + id)));
    }

    public Mono<Character> getOwnedCharacter(String id, String ownerId) {
        return characterRepository.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "Couldn't find any character with id " + id + " on this account.")));
    }

    public Mono<Character> getCharacterByName(String name) {
        return characterRepository.findByName(name)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with name: " + name)));
    }

    public Mono<Character> createCharacter(CreateCharacterRequest request, String ownerId) {
        return rosterPolicy.checkHasRoom(ownerId)
                .then(Mono.fromCallable(() -> characterFactories.create(request)))
                .map(character -> {
                    character.setOwnerId(ownerId);
                    return character;
                })
                .flatMap(characterRepository::save);
    }

    public Mono<Character> updateCharacter(String id, String ownerId, Character character) {
        return getOwnedCharacter(id, ownerId)
                .flatMap(existingCharacter -> {
                    character.setId(id);
                    character.setOwnerId(ownerId);
                    return characterRepository.save(character);
                });
    }

    public Mono<Void> deleteCharacter(String id, String ownerId) {
        return getOwnedCharacter(id, ownerId)
                .flatMap(existingCharacter -> characterRepository.deleteById(id));
    }

    public Flux<Character> getCharactersByClass(CharacterClass characterClass) {
        return characterRepository.findByCharacterClass(characterClass.toString());
    }
}
