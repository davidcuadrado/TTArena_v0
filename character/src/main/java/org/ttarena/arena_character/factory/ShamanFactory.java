package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Shaman;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;

@Component
public class ShamanFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.SHAMAN;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Shaman(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(ShamanSpecialization.class, request.specialization()));
    }
}
