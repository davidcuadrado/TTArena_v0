package org.ttarena.arena_user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ttarena.arena_user.model.Character;
import org.ttarena.arena_user.model.Warrior;
import org.ttarena.arena_user.model.Priest;
import org.ttarena.arena_user.model.Paladin;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.WarriorSpecialization;
import org.ttarena.arena_user.model.enums.PriestSpecialization;
import org.ttarena.arena_user.model.enums.PaladinSpecialization;
import org.ttarena.arena_user.repository.CharacterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que gestiona las operaciones relacionadas con los personajes.
 */
@Service
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    
    @Autowired
    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }
    
    /**
     * Obtiene todos los personajes.
     * 
     * @return Un Flux que emite todos los personajes
     */
    public Flux<Character> getAllCharacters() {
        return characterRepository.findAll();
    }
    
    /**
     * Obtiene un personaje por su ID.
     * 
     * @param id ID del personaje
     * @return Un Mono que emite el personaje encontrado o vacío si no existe
     */
    public Mono<Character> getCharacterById(String id) {
        return characterRepository.findById(id);
    }
    
    /**
     * Obtiene un personaje por su nombre.
     * 
     * @param name Nombre del personaje
     * @return Un Mono que emite el personaje encontrado o vacío si no existe
     */
    public Mono<Character> getCharacterByName(String name) {
        return characterRepository.findByName(name);
    }
    
    /**
     * Crea un nuevo guerrero.
     * 
     * @param name Nombre del guerrero
     * @param health Salud del guerrero
     * @param rage Rabia del guerrero
     * @param specialization Especialización del guerrero
     * @return Un Mono que emite el guerrero creado
     */
    public Mono<Warrior> createWarrior(String name, int health, int rage, WarriorSpecialization specialization) {
        Warrior warrior = new Warrior(name, health, rage, specialization);
        return characterRepository.save(warrior).cast(Warrior.class);
    }
    
    /**
     * Crea un nuevo sacerdote.
     * 
     * @param name Nombre del sacerdote
     * @param health Salud del sacerdote
     * @param mana Maná del sacerdote
     * @param specialization Especialización del sacerdote
     * @return Un Mono que emite el sacerdote creado
     */
    public Mono<Priest> createPriest(String name, int health, int mana, PriestSpecialization specialization) {
        Priest priest = new Priest(name, health, mana, specialization);
        return characterRepository.save(priest).cast(Priest.class);
    }
    
    /**
     * Crea un nuevo paladín.
     * 
     * @param name Nombre del paladín
     * @param health Salud del paladín
     * @param holyPower Poder sagrado del paladín
     * @param specialization Especialización del paladín
     * @return Un Mono que emite el paladín creado
     */
    public Mono<Paladin> createPaladin(String name, int health, int holyPower, PaladinSpecialization specialization) {
        Paladin paladin = new Paladin(name, health, holyPower, specialization);
        return characterRepository.save(paladin).cast(Paladin.class);
    }
    
    /**
     * Actualiza un personaje existente.
     * 
     * @param id ID del personaje a actualizar
     * @param character Personaje con los datos actualizados
     * @return Un Mono que emite el personaje actualizado o vacío si no existe
     */
    public Mono<Character> updateCharacter(String id, Character character) {
        return characterRepository.findById(id)
                .flatMap(existingCharacter -> {
                    character.setId(id);
                    return characterRepository.save(character);
                });
    }
    
    /**
     * Elimina un personaje por su ID.
     * 
     * @param id ID del personaje a eliminar
     * @return Un Mono que emite vacío cuando se completa la eliminación
     */
    public Mono<Void> deleteCharacter(String id) {
        return characterRepository.deleteById(id);
    }
    
    /**
     * Obtiene todos los personajes de una clase específica.
     * 
     * @param characterClass Clase de personaje
     * @return Un Flux que emite los personajes de la clase especificada
     */
    public Flux<Character> getCharactersByClass(CharacterClass characterClass) {
        return characterRepository.findByCharacterClass(characterClass.toString());
    }
}
