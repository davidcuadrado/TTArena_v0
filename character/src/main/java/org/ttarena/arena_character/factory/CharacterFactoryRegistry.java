package org.ttarena.arena_character.factory;

import org.springframework.stereotype.Component;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.enums.CharacterClass;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Indexes every {@link CharacterFactory} bean by the class it supports and
 * dispatches creation requests to the right one.
 *
 * <p>Completeness is checked at startup: if a {@link CharacterClass} has no
 * factory, or two factories claim the same class, the context fails to start.
 * That replaces the compile-time safety the old one-method-per-class service
 * had - a new enum constant without a factory is caught immediately rather
 * than at the first request for it.
 */
@Component
public class CharacterFactoryRegistry {

    private final Map<CharacterClass, CharacterFactory> factories = new EnumMap<>(CharacterClass.class);

    public CharacterFactoryRegistry(List<CharacterFactory> availableFactories) {
        for (CharacterFactory factory : availableFactories) {
            CharacterFactory previous = factories.put(factory.supports(), factory);
            if (previous != null) {
                throw new IllegalStateException("Two factories claim " + factory.supports() + ": "
                        + previous.getClass().getName() + " and " + factory.getClass().getName());
            }
        }

        Set<CharacterClass> missing = EnumSet.allOf(CharacterClass.class);
        missing.removeAll(factories.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalStateException("No CharacterFactory registered for: " + missing);
        }
    }

    public Character create(CreateCharacterRequest request) {
        CharacterFactory factory = factories.get(request.characterClass());
        if (factory == null) {
            throw new BadRequestException("Unsupported character class: " + request.characterClass() + ".");
        }
        return factory.create(request);
    }

    public Set<CharacterClass> supportedClasses() {
        return Set.copyOf(factories.keySet());
    }
}
