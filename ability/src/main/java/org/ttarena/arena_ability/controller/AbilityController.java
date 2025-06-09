package org.ttarena.arena_ability.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_ability.model.Ability;
import org.ttarena.arena_ability.model.AbilityType;
import org.ttarena.arena_ability.model.Specialization;
import org.ttarena.arena_ability.model.WowClass;
import org.ttarena.arena_ability.service.AbilityServiceInterface;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/abilities")
@RequiredArgsConstructor
@Validated
public class AbilityController {
    
    private final AbilityServiceInterface abilityServiceInterface;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAllAbilities() {
        log.info("GET /api/v1/abilities - Getting all abilities");
        return abilityServiceInterface.getAllAbilities()
                .doOnComplete(() -> log.info("Successfully retrieved all abilities"));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Ability>> getAbilityById(@PathVariable String id) {
        log.info("GET /api/v1/abilities/{} - Getting ability by ID", id);
        return abilityServiceInterface.getAbilityById(id)
                .map(ability -> {
                    log.info("Successfully found ability: {}", ability.getName());
                    return ResponseEntity.ok(ability);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Ability>> getAbilityByName(@PathVariable String name) {
        log.info("GET /api/v1/abilities/name/{} - Getting ability by name", name);
        return abilityServiceInterface.getAbilityByName(name)
                .map(ability -> {
                    log.info("Successfully found ability: {}", ability.getName());
                    return ResponseEntity.ok(ability);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/class/{wowClass}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAbilitiesByClass(@PathVariable WowClass wowClass) {
        log.info("GET /api/v1/abilities/class/{} - Getting abilities by class", wowClass);
        return abilityServiceInterface.getAbilitiesByClass(wowClass)
                .doOnComplete(() -> log.info("Successfully retrieved abilities for class: {}", wowClass));
    }

    @GetMapping(value = "/specialization/{specialization}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAbilitiesBySpecialization(@PathVariable Specialization specialization) {
        log.info("GET /api/v1/abilities/specialization/{} - Getting abilities by specialization", specialization);
        return abilityServiceInterface.getAbilitiesBySpecialization(specialization)
                .doOnComplete(() -> log.info("Successfully retrieved abilities for specialization: {}", specialization));
    }

    @GetMapping(value = "/class/{wowClass}/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAllAbilitiesForClass(@PathVariable WowClass wowClass) {
        log.info("GET /api/v1/abilities/class/{}/all - Getting all abilities for class", wowClass);
        return abilityServiceInterface.getAllAbilitiesForClass(wowClass)
                .doOnComplete(() -> log.info("Successfully retrieved all abilities for class: {}", wowClass));
    }

    @GetMapping(value = "/type/{abilityType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAbilitiesByType(@PathVariable AbilityType abilityType) {
        log.info("GET /api/v1/abilities/type/{} - Getting abilities by type", abilityType);
        return abilityServiceInterface.getAbilitiesByType(abilityType)
                .doOnComplete(() -> log.info("Successfully retrieved abilities for type: {}", abilityType));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> searchAbilities(@RequestParam String q) {
        log.info("GET /api/v1/abilities/search?q={} - Searching abilities", q);
        return abilityServiceInterface.searchAbilities(q)
                .doOnComplete(() -> log.info("Successfully completed search for: {}", q));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Ability>> createAbility(@Valid @RequestBody Ability ability) {
        log.info("POST /api/v1/abilities - Creating ability: {}", ability.getName());
        return abilityServiceInterface.createAbility(ability)
                .map(createdAbility -> {
                    log.info("Successfully created ability: {}", createdAbility.getName());
                    return ResponseEntity.status(HttpStatus.CREATED).body(createdAbility);
                })
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Ability>> updateAbility(@PathVariable String id, @Valid @RequestBody Ability ability) {
        log.info("PUT /api/v1/abilities/{} - Updating ability", id);
        return abilityServiceInterface.updateAbility(id, ability)
                .map(updatedAbility -> {
                    log.info("Successfully updated ability: {}", updatedAbility.getName());
                    return ResponseEntity.ok(updatedAbility);
                })
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAbility(@PathVariable String id) {
        log.info("DELETE /api/v1/abilities/{} - Deleting ability", id);
        return abilityServiceInterface.deleteAbility(id)
                .then(Mono.fromCallable(() -> {
                    log.info("Successfully deleted ability with ID: {}", id);
                    return ResponseEntity.noContent().<Void>build();
                }))
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/exists/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> existsByName(@PathVariable String name) {
        log.info("GET /api/v1/abilities/exists/{} - Checking if ability exists", name);
        return abilityServiceInterface.existsByName(name)
                .map(exists -> {
                    log.info("Ability '{}' exists: {}", name, exists);
                    return ResponseEntity.ok(exists);
                });
    }
}

