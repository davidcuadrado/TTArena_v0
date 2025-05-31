package org.ttarena.arena_user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_user.model.Character;
import org.ttarena.arena_user.model.Warrior;
import org.ttarena.arena_user.model.Priest;
import org.ttarena.arena_user.model.Paladin;
import org.ttarena.arena_user.model.enums.CharacterClass;
import org.ttarena.arena_user.model.enums.WarriorSpecialization;
import org.ttarena.arena_user.model.enums.PriestSpecialization;
import org.ttarena.arena_user.model.enums.PaladinSpecialization;
import org.ttarena.arena_user.service.CharacterService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para gestionar las operaciones relacionadas con los personajes.
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {
    
    private final CharacterService characterService;
    
    @Autowired
    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    /**
     * Obtiene todos los personajes.
     * 
     * @return Un Flux que emite todos los personajes
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getAllCharacters() {
        return characterService.getAllCharacters();
    }
    
    /**
     * Obtiene un personaje por su ID.
     * 
     * @param id ID del personaje
     * @return Un Mono que emite el personaje encontrado
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> getCharacterById(@PathVariable String id) {
        return characterService.getCharacterById(id);
    }
    
    /**
     * Obtiene un personaje por su nombre.
     * 
     * @param name Nombre del personaje
     * @return Un Mono que emite el personaje encontrado
     */
    @GetMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> getCharacterByName(@PathVariable String name) {
        return characterService.getCharacterByName(name);
    }
    
    /**
     * Obtiene todos los personajes de una clase específica.
     * 
     * @param characterClass Clase de personaje
     * @return Un Flux que emite los personajes de la clase especificada
     */
    @GetMapping(value = "/class/{characterClass}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Character> getCharactersByClass(@PathVariable CharacterClass characterClass) {
        return characterService.getCharactersByClass(characterClass);
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
    @PostMapping(value = "/warrior", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Warrior> createWarrior(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int rage,
            @RequestParam WarriorSpecialization specialization) {
        return characterService.createWarrior(name, health, rage, specialization);
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
    @PostMapping(value = "/priest", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Priest> createPriest(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int mana,
            @RequestParam PriestSpecialization specialization) {
        return characterService.createPriest(name, health, mana, specialization);
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
    @PostMapping(value = "/paladin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paladin> createPaladin(
            @RequestParam String name,
            @RequestParam int health,
            @RequestParam int holyPower,
            @RequestParam PaladinSpecialization specialization) {
        return characterService.createPaladin(name, health, holyPower, specialization);
    }
    
    /**
     * Actualiza un personaje existente.
     * 
     * @param id ID del personaje a actualizar
     * @param character Personaje con los datos actualizados
     * @return Un Mono que emite el personaje actualizado
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Character> updateCharacter(
            @PathVariable String id,
            @RequestBody Character character) {
        return characterService.updateCharacter(id, character);
    }
    
    /**
     * Elimina un personaje por su ID.
     * 
     * @param id ID del personaje a eliminar
     * @return Un Mono que emite vacío cuando se completa la eliminación
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCharacter(@PathVariable String id) {
        return characterService.deleteCharacter(id);
    }
}
