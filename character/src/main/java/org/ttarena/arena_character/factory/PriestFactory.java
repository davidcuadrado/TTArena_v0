package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PriestSpecialization;

@Component
public class PriestFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.PRIEST;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Priest(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(PriestSpecialization.class, request.specialization()));
    }
}
