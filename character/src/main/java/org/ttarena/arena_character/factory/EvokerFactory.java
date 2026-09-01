package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Evoker;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.EvokerSpecialization;

@Component
public class EvokerFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.EVOKER;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Evoker(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(EvokerSpecialization.class, request.specialization()));
    }
}
