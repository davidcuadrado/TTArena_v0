package org.ttarena.arena_character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.Paladin;
import org.ttarena.arena_character.model.Rogue;
import org.ttarena.arena_character.model.Shaman;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.RogueSpecialization;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    
    @Autowired
    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public Flux<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    public Mono<Character> getCharacterById(String id) {
        return characterRepository.findById(id);
    }

    public Mono<Character> getCharacterByName(String name) {
        return characterRepository.findByName(name);
    }

    public Mono<Warrior> createWarrior(String name, int health, int rage, WarriorSpecialization specialization) {
        Warrior warrior = new Warrior(name, health, rage, specialization);
        return characterRepository.save(warrior).cast(Warrior.class);
    }

    public Mono<Priest> createPriest(String name, int health, int mana, PriestSpecialization specialization) {
        Priest priest = new Priest(name, health, mana, specialization);
        return characterRepository.save(priest).cast(Priest.class);
    }

    public Mono<Paladin> createPaladin(String name, int health, int holyPower, PaladinSpecialization specialization) {
        Paladin paladin = new Paladin(name, health, holyPower, specialization);
        return characterRepository.save(paladin).cast(Paladin.class);
    }

    public Mono<Rogue> createRogue(String name, int health, int energy, RogueSpecialization specialization) {
        Rogue rogue = new Rogue(name, health, energy, specialization);
        return characterRepository.save(rogue).cast(Rogue.class);
    }

    public Mono<Shaman> createShaman(String name, int health, int mana, ShamanSpecialization specialization) {
        Shaman shaman = new Shaman(name, health, mana, specialization);
        return characterRepository.save(shaman).cast(Shaman.class);
    }

    public Mono<Character> updateCharacter(String id, Character character) {
        return characterRepository.findById(id)
                .flatMap(existingCharacter -> {
                    character.setId(id);
                    return characterRepository.save(character);
                });
    }

    public Mono<Void> deleteCharacter(String id) {
        return characterRepository.deleteById(id);
    }

    public Flux<Character> getCharactersByClass(CharacterClass characterClass) {
        return characterRepository.findByCharacterClass(characterClass.toString());
    }
}
