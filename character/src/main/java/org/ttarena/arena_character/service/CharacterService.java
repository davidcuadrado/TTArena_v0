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

    public CharacterService(CharacterRepository characterRepository,
                            CharacterFactoryRegistry characterFactories) {
        this.characterRepository = characterRepository;
        this.characterFactories = characterFactories;
    }

    public Flux<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    public Mono<Character> getCharacterById(String id) {
        return characterRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + id)));
    }

    public Mono<Character> getCharacterByName(String name) {
        return characterRepository.findByName(name)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with name: " + name)));
    }

    /**
     * Creates a character of whichever class the request names. The concrete
     * type is chosen by the matching {@link org.ttarena.arena_character.factory.CharacterFactory},
     * so this method never needs to change when a class is added.
     *
     * <p>Creation runs inside {@code fromCallable} so that a rejected request
     * (unknown specialization, for instance) surfaces as an error signal rather
     * than being thrown while the pipeline is being assembled.
     */
    public Mono<Character> createCharacter(CreateCharacterRequest request) {
        return Mono.fromCallable(() -> characterFactories.create(request))
                .flatMap(characterRepository::save);
    }

    public Mono<Character> updateCharacter(String id, Character character) {
        return characterRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + id)))
                .flatMap(existingCharacter -> {
                    character.setId(id);
                    return characterRepository.save(character);
                });
    }

    public Mono<Void> deleteCharacter(String id) {
        return characterRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + id)))
                .flatMap(existingCharacter -> characterRepository.deleteById(id));
    }

    public Flux<Character> getCharactersByClass(CharacterClass characterClass) {
        return characterRepository.findByCharacterClass(characterClass.toString());
    }
}
