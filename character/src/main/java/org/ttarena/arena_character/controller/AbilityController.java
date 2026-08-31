package org.ttarena.arena_character.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.CombatResult;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.service.AbilityService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/abilities")
public class AbilityController {

    private final AbilityService abilityService;

    @Autowired
    public AbilityController(AbilityService abilityService) {
        this.abilityService = abilityService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAllAbilities() {
        return abilityService.getAllAbilities();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Ability> getAbilityById(@PathVariable String id) {
        return abilityService.getAbilityById(id);
    }

    @GetMapping(value = "/class/{characterClass}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAbilitiesByClass(@PathVariable CharacterClass characterClass) {
        return abilityService.getAbilitiesByClass(characterClass);
    }

    @GetMapping(value = "/class/{characterClass}/specialization/{specialization}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Ability> getAbilitiesByClassAndSpecialization(
            @PathVariable CharacterClass characterClass,
            @PathVariable String specialization) {
        return abilityService.getAbilitiesByClassAndSpecialization(characterClass, specialization.toUpperCase());
    }

    /**
     * Casts an ability: resolves its damage/healing against the given
     * targets and persists the resulting state. Body fields:
     * casterId, abilityId, targetIds (omit or empty for SELF-targeted abilities).
     */
    @PostMapping(value = "/cast", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CombatResult> castAbility(@RequestBody CastAbilityRequest request) {
        return abilityService.castAbility(request.casterId(), request.abilityId(), request.targetIds());
    }

    public record CastAbilityRequest(String casterId, String abilityId, List<String> targetIds) {
    }
}
