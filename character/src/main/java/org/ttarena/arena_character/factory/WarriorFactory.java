package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;

@Component
public class WarriorFactory implements CharacterFactory {

    @Override
    public CharacterClass supports() {
        return CharacterClass.WARRIOR;
    }

    @Override
    public Character create(CreateCharacterRequest request) {
        return new Warrior(
                request.name(),
                request.health(),
                request.resourceAmount(),
                specialization(WarriorSpecialization.class, request.specialization()));
    }
}
