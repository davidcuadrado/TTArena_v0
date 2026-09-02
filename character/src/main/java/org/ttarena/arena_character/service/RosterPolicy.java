package org.ttarena.arena_character.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Mono;

@Component
public class RosterPolicy {

    private final CharacterRepository characterRepository;
    private final int maxSize;

    public RosterPolicy(CharacterRepository characterRepository,
                        @Value("${character.roster.max-size:10}") int maxSize) {
        this.characterRepository = characterRepository;
        this.maxSize = maxSize;
    }

    public int maxSize() {
        return maxSize;
    }

    public Mono<Void> checkHasRoom(String ownerId) {
        return characterRepository.countByOwnerId(ownerId)
                .flatMap(count -> count >= maxSize
                        ? Mono.error(new BadRequestException(
                                "Roster is full: an account may hold at most " + maxSize + " characters."))
                        : Mono.empty());
    }
}
