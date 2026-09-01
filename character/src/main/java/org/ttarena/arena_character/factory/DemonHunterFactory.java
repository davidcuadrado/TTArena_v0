package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.DemonHunter;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DemonHunterSpecialization;

@Component
public class DemonHunterFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.DEMON_HUNTER;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new DemonHunter(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(DemonHunterSpecialization.class, request.specialization()));
    }
}
