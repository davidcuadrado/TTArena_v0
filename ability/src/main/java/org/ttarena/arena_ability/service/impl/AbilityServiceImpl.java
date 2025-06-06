package org.ttarena.arena_ability.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import org.ttarena.arena_ability.repository.AbilityRepository;
import org.ttarena.arena_ability.service.AbilityService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityServiceImpl implements AbilityService {
    
    private final AbilityRepository abilityRepository;
    
    @Override
    public List<Ability> getAllAbilities() {
        log.debug("Returns all abilities ");
        return abilityRepository.findAll();
    }
    
    @Override
    public Optional<Ability> getAbilityById(String id) {
        log.debug("Retrieving ability by ID: {}", id);
        return abilityRepository.findById(id);
    }
    
    @Override
    public Optional<Ability> getAbilityByName(String name) {
        log.debug("Retrieving ability by name: {}", name);
        return abilityRepository.findByNameIgnoreCase(name);
    }
    
    @Override
    public List<Ability> getAbilitiesByClass(WowClass wowClass) {
        log.debug("Retrieving abilities by class: {}", wowClass);
        return abilityRepository.findByWowClass(wowClass);
    }
    
    @Override
    public List<Ability> getAbilitiesBySpecialization(Specialization specialization) {
        log.debug("Retrieving abilities by specialization: {}", specialization);
        return abilityRepository.findBySpecialization(specialization);
    }
    
    @Override
    public List<Ability> getAllAbilitiesForClassAndSpecs(WowClass wowClass) {
        log.debug("Retrieving all abilities for a class and specialization: {}", wowClass);

        List<Specialization> classSpecializations = Arrays.stream(Specialization.values())
                .filter(spec -> spec.getWowClass() == wowClass)
                .toList();
        
        return abilityRepository.findByClassOrSpecializations(wowClass, classSpecializations);
    }
    
    @Override
    public List<Ability> getAbilitiesByType(AbilityType abilityType) {
        log.debug("Retrieving abilities by type: {}", abilityType);
        return abilityRepository.findByAbilityType(abilityType);
    }
    
    @Override
    public List<Ability> searchAbilities(String searchText) {
        log.debug("Retrieving abilities by text: {}", searchText);
        return abilityRepository.findByNameOrDescriptionContaining(searchText);
    }
    
    @Override
    public Ability createAbility(Ability ability) {
        log.debug("Creating new ability: {}", ability.getName());

        Optional<Ability> existingAbility = abilityRepository.findByNameIgnoreCase(ability.getName());
        if (existingAbility.isPresent()) {
            throw new IllegalArgumentException("An ability with that name already exists: " + ability.getName());
        }
        
        return abilityRepository.save(ability);
    }
    
    @Override
    public Optional<Ability> updateAbility(String id, Ability ability) {
        log.debug("Updating ability with ID: {}", id);
        
        return abilityRepository.findById(id)
                .map(existingAbility -> {
                    Optional<Ability> abilityWithSameName = abilityRepository.findByNameIgnoreCase(ability.getName());
                    if (abilityWithSameName.isPresent() && !abilityWithSameName.get().getId().equals(id)) {
                        throw new IllegalArgumentException("An ability with the same name already exists: " + ability.getName());
                    }
                    
                    // Actualizar los campos
                    existingAbility.setName(ability.getName());
                    existingAbility.setDescription(ability.getDescription());
                    existingAbility.setIconUrl(ability.getIconUrl());
                    existingAbility.setCooldown(ability.getCooldown());
                    existingAbility.setCastTime(ability.getCastTime());
                    existingAbility.setResourceCost(ability.getResourceCost());
                    existingAbility.setResourceType(ability.getResourceType());
                    existingAbility.setAbilityType(ability.getAbilityType());
                    existingAbility.setWowClass(ability.getWowClass());
                    existingAbility.setSpecialization(ability.getSpecialization());
                    existingAbility.setBaseValue(ability.getBaseValue());
                    existingAbility.setRange(ability.getRange());
                    existingAbility.setAreaEffect(ability.isAreaEffect());
                    existingAbility.setAreaRadius(ability.getAreaRadius());
                    
                    return abilityRepository.save(existingAbility);
                });
    }
    
    @Override
    public boolean deleteAbility(String id) {
        log.debug("Deleting ability by ID: {}", id);
        
        if (abilityRepository.existsById(id)) {
            abilityRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

