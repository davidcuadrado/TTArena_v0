package org.ttarena.arena_map.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ttarena.arena_map.document.GameMap;
import reactor.core.publisher.Flux;

/**
 * Repositorio reactivo para la entidad GameMap.
 * Proporciona operaciones CRUD reactivas para la persistencia de mapas en MongoDB.
 */
@Repository
public interface GameMapRepository extends ReactiveMongoRepository<GameMap, String> {
    
    /**
     * Busca mapas por nombre (búsqueda parcial, no sensible a mayúsculas/minúsculas)
     * 
     * @param name Nombre o parte del nombre a buscar
     * @return Flujo de mapas que coinciden con el criterio de búsqueda
     */
    Flux<GameMap> findByNameContainingIgnoreCase(String name);
    
    /**
     * Busca mapas por autor
     * 
     * @param author Nombre del autor
     * @return Flujo de mapas creados por el autor especificado
     */
    Flux<GameMap> findByAuthor(String author);
}
