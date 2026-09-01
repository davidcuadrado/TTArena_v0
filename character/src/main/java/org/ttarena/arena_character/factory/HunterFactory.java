package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Hunter;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.HunterSpecialization;

@Component
public class HunterFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.HUNTER;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Hunter(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(HunterSpecialization.class, request.specialization()));
    }
}
