package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.DeathKnight;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DeathKnightSpecialization;

@Component
public class DeathKnightFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.DEATH_KNIGHT;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new DeathKnight(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(DeathKnightSpecialization.class, request.specialization()));
    }
}
