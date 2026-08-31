package org.ttarena.arena_character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import org.ttarena.arena_character.exception.NotFoundException;
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
        return characterRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with id: " + id)));
    }

    public Mono<Character> getCharacterByName(String name) {
        return characterRepository.findByName(name)
                .switchIfEmpty(Mono.error(new NotFoundException("Couldn't find any character with name: " + name)));
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

    public Mono<Hunter> createHunter(String name, int health, int focus, HunterSpecialization specialization) {
        Hunter hunter = new Hunter(name, health, focus, specialization);
        return characterRepository.save(hunter).cast(Hunter.class);
    }

    public Mono<DeathKnight> createDeathKnight(String name, int health, int runicPower, DeathKnightSpecialization specialization) {
        DeathKnight deathKnight = new DeathKnight(name, health, runicPower, specialization);
        return characterRepository.save(deathKnight).cast(DeathKnight.class);
    }

    public Mono<Mage> createMage(String name, int health, int mana, MageSpecialization specialization) {
        Mage mage = new Mage(name, health, mana, specialization);
        return characterRepository.save(mage).cast(Mage.class);
    }

    public Mono<Warlock> createWarlock(String name, int health, int mana, WarlockSpecialization specialization) {
        Warlock warlock = new Warlock(name, health, mana, specialization);
        return characterRepository.save(warlock).cast(Warlock.class);
    }

    public Mono<Monk> createMonk(String name, int health, int energy, MonkSpecialization specialization) {
        Monk monk = new Monk(name, health, energy, specialization);
        return characterRepository.save(monk).cast(Monk.class);
    }

    public Mono<Druid> createDruid(String name, int health, int mana, DruidSpecialization specialization) {
        Druid druid = new Druid(name, health, mana, specialization);
        return characterRepository.save(druid).cast(Druid.class);
    }

    public Mono<DemonHunter> createDemonHunter(String name, int health, int fury, DemonHunterSpecialization specialization) {
        DemonHunter demonHunter = new DemonHunter(name, health, fury, specialization);
        return characterRepository.save(demonHunter).cast(DemonHunter.class);
    }

    public Mono<Evoker> createEvoker(String name, int health, int essence, EvokerSpecialization specialization) {
        Evoker evoker = new Evoker(name, health, essence, specialization);
        return characterRepository.save(evoker).cast(Evoker.class);
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
