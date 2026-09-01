package org.ttarena.arena_character.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.factory.CharacterFactory;
import org.ttarena.arena_character.factory.CharacterFactoryRegistry;
import org.ttarena.arena_character.factory.DeathKnightFactory;
import org.ttarena.arena_character.factory.DemonHunterFactory;
import org.ttarena.arena_character.factory.DruidFactory;
import org.ttarena.arena_character.factory.EvokerFactory;
import org.ttarena.arena_character.factory.HunterFactory;
import org.ttarena.arena_character.factory.MageFactory;
import org.ttarena.arena_character.factory.MonkFactory;
import org.ttarena.arena_character.factory.PaladinFactory;
import org.ttarena.arena_character.factory.PriestFactory;
import org.ttarena.arena_character.factory.RogueFactory;
import org.ttarena.arena_character.factory.ShamanFactory;
import org.ttarena.arena_character.factory.WarlockFactory;
import org.ttarena.arena_character.factory.WarriorFactory;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Druid;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {
    private static final List<CharacterFactory> ALL_FACTORIES = List.of(
            new WarriorFactory(), new PriestFactory(), new PaladinFactory(), new RogueFactory(),
            new ShamanFactory(), new HunterFactory(), new DeathKnightFactory(), new MageFactory(),
            new WarlockFactory(), new MonkFactory(), new DruidFactory(), new DemonHunterFactory(),
            new EvokerFactory());

    @Mock
    private CharacterRepository characterRepository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(characterRepository,
                new CharacterFactoryRegistry(ALL_FACTORIES));
    }

    @Test
    void createsTheConcreteTypeForTheRequestedClass() {
        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Character created = characterService.createCharacter(
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "ARMS")).block();

        assertThat(created).isInstanceOf(Warrior.class);
        assertThat(created.getName()).isEqualTo("Conan");
        assertThat(created.getStatValue(StatType.STRENGTH)).isEqualTo(100);
    }

    @Test
    void createsHybridClassesWithBothStats() {
        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Character created = characterService.createCharacter(
                new CreateCharacterRequest("Malfurion", CharacterClass.DRUID, 200, 190, "BALANCE")).block();

        assertThat(created).isInstanceOf(Druid.class);
        assertThat(created.getStatValue(StatType.INTELLECT)).isEqualTo(120);
        assertThat(created.getStatValue(StatType.AGILITY)).isEqualTo(50);
    }

    @Test
    void doesNotPersistAnythingWhenTheSpecializationIsInvalid() {
        StepVerifier.create(characterService.createCharacter(
                        new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "NOT_A_SPEC")))
                .expectError(BadRequestException.class)
                .verify();

        verify(characterRepository, never()).save(any(Character.class));
    }
}
