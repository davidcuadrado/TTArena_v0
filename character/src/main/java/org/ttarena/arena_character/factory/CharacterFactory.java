package org.ttarena.arena_character.factory;

import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Creates characters of one specific {@link CharacterClass}.
 *
 * <p>Every implementation is a Spring bean, and {@link CharacterFactoryRegistry}
 * collects them into a map keyed by {@link #supports()}. Supporting a new class
 * therefore means adding one implementation of this interface - no changes to
 * the service, the controller, or any existing factory.
 */
public interface CharacterFactory {

    /** The class this factory builds. Must be unique across all factories. */
    CharacterClass supports();

    Character create(CreateCharacterRequest request);

    /**
     * Parses the raw specialization string from the request into this class's
     * own specialization enum, rejecting anything unknown with a message that
     * lists the valid values.
     */
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
