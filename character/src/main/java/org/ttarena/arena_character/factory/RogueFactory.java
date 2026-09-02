package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Rogue;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.RogueSpecialization;

@Component
public class RogueFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.ROGUE;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Rogue(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(RogueSpecialization.class, request.specialization()));
    }
}
