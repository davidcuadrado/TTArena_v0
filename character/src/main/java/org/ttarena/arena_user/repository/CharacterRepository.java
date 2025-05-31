package org.ttarena.arena_user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_user.model.Character;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad Character.
 * Proporciona operaciones CRUD reactivas para todos los personajes.
 */
@Repository
public interface CharacterRepository extends ReactiveMongoRepository<Character, String> {
    
    /**
     * Busca un personaje por su nombre.
     * 
     * @param name Nombre del personaje
     * @return Un Mono que emite el personaje encontrado o vacío si no existe
     */
    Mono<Character> findByName(String name);
    
    /**
     * Busca todos los personajes por su clase.
     * 
     * @param characterClass Clase del personaje
     * @return Un Flux que emite los personajes encontrados
     */
    Flux<Character> findByCharacterClass(String characterClass);
}
