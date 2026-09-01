package org.ttarena.arena_character.factory;

import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public interface CharacterFactory {
    CharacterClass supports();

    Character create(CreateCharacterRequest request);

    default <E extends Enum<E>> E specialization(Class<E> type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("A specialization is required for " + supports() + ".");
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(type.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new BadRequestException(
                    "Unknown specialization '" + raw + "' for " + supports() + ". Valid values: " + valid + ".");
        }
    }
}
