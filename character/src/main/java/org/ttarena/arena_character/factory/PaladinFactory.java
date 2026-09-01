package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Paladin;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;

@Component
public class PaladinFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.PALADIN;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Paladin(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(PaladinSpecialization.class, request.specialization()));
    }
}
