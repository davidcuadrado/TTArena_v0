package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warlock;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.WarlockSpecialization;

@Component
public class WarlockFactory implements CharacterFactory {
    @Override
    public CharacterClass supports() {
        return CharacterClass.WARLOCK;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Warlock(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(WarlockSpecialization.class, request.specialization()));
    }
}
