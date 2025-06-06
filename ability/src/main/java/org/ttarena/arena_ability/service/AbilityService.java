package org.ttarena.arena_ability.service;

import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para gestionar las habilidades
 */
public interface AbilityService {
    
    /**
     * Obtiene todas las habilidades
     */
    List<Ability> getAllAbilities();
    
    /**
     * Obtiene una habilidad por su ID
     */
    Optional<Ability> getAbilityById(String id);
    
    /**
     * Obtiene una habilidad por su nombre
     */
    Optional<Ability> getAbilityByName(String name);
    
    /**
     * Obtiene todas las habilidades de una clase específica
     */
    List<Ability> getAbilitiesByClass(WowClass wowClass);
    
    /**
     * Obtiene todas las habilidades de una especialización específica
     */
    List<Ability> getAbilitiesBySpecialization(Specialization specialization);
    
    /**
     * Obtiene todas las habilidades disponibles para una clase y sus especializaciones
     */
    List<Ability> getAllAbilitiesForClassAndSpecs(WowClass wowClass);
    
    /**
     * Obtiene todas las habilidades de un tipo específico
     */
    List<Ability> getAbilitiesByType(AbilityType abilityType);
    
    /**
     * Busca habilidades que contengan un texto en el nombre o descripción
     */
    List<Ability> searchAbilities(String searchText);
    
    /**
     * Crea una nueva habilidad
     */
    Ability createAbility(Ability ability);
    
    /**
     * Actualiza una habilidad existente
     */
    Optional<Ability> updateAbility(String id, Ability ability);
    
    /**
     * Elimina una habilidad por su ID
     */
    boolean deleteAbility(String id);
}

