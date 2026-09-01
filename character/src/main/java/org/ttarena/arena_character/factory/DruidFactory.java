package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Druid;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.DruidSpecialization;

@Component
public class DruidFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.DRUID;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Druid(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(DruidSpecialization.class, request.specialization()));
    }
}
