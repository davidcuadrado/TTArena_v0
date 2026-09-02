package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Monk;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.MonkSpecialization;

@Component
public class MonkFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.MONK;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Monk(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(MonkSpecialization.class, request.specialization()));
    }
}
