package org.ttarena.arena_ability.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import org.ttarena.arena_ability.service.AbilityService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/abilities")
@RequiredArgsConstructor
@Validated
public class AbilityController {
    
    private final AbilityService abilityService;

    @GetMapping
    public ResponseEntity<List<Ability>> getAllAbilities() {
        log.info("Retrieving all abilities");
        List<Ability> abilities = abilityService.getAllAbilities();
        return ResponseEntity.ok(abilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ability> getAbilityById(@PathVariable String id) {
        log.info("Retrieving ability ID: {}", id);
        return abilityService.getAbilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Ability> getAbilityByName(@PathVariable String name) {
        log.info("Retrieving ability name: {}", name);
        return abilityService.getAbilityByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{wowClass}")
    public ResponseEntity<List<Ability>> getAbilitiesByClass(@PathVariable WowClass wowClass) {
        log.info("Retrieving abilities for class: {}", wowClass);
        List<Ability> abilities = abilityService.getAbilitiesByClass(wowClass);
        return ResponseEntity.ok(abilities);
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Ability>> getAbilitiesBySpecialization(@PathVariable Specialization specialization) {
        log.info("Retrieving abilities for specialization: {}", specialization);
        List<Ability> abilities = abilityService.getAbilitiesBySpecialization(specialization);
        return ResponseEntity.ok(abilities);
    }

    @GetMapping("/class/{wowClass}/all")
    public ResponseEntity<List<Ability>> getAllAbilitiesForClass(@PathVariable WowClass wowClass) {
        log.info("Retrieving all abilities for class: {}", wowClass);
        List<Ability> abilities = abilityService.getAllAbilitiesForClassAndSpecs(wowClass);
        return ResponseEntity.ok(abilities);
    }

    @GetMapping("/type/{abilityType}")
    public ResponseEntity<List<Ability>> getAbilitiesByType(@PathVariable AbilityType abilityType) {
        log.info("Retrieving abilities by type: {}", abilityType);
        List<Ability> abilities = abilityService.getAbilitiesByType(abilityType);
        return ResponseEntity.ok(abilities);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ability>> searchAbilities(@RequestParam String q) {
        log.info("Retrieving abilities by text: {}", q);
        List<Ability> abilities = abilityService.searchAbilities(q);
        return ResponseEntity.ok(abilities);
    }

    @PostMapping
    public ResponseEntity<Ability> createAbility(@Valid @RequestBody Ability ability) {
        log.info("Creating new ability: {}", ability.getName());
        try {
            Ability createdAbility = abilityService.createAbility(ability);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAbility);
        } catch (IllegalArgumentException e) {
            log.error("Error creating new ability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ability> updateAbility(@PathVariable String id, @Valid @RequestBody Ability ability) {
        log.info("Update ability with ID: {}", id);
        try {
            return abilityService.updateAbility(id, ability)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Error updating ability with ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAbility(@PathVariable String id) {
        log.info("Deleting ability with ID: {}", id);
        boolean deleted = abilityService.deleteAbility(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

