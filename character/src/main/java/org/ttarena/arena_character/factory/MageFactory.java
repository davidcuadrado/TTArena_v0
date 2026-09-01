package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Mage;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.MageSpecialization;

@Component
public class MageFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.MAGE;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Mage(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(MageSpecialization.class, request.specialization()));
    }
}
