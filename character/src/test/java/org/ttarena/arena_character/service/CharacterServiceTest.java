package org.ttarena.arena_character.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.exception.NotFoundException;
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
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.repository.CharacterRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    private static final String OWNER = "owner-1";
    private static final String OTHER_OWNER = "owner-2";

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
                new CharacterFactoryRegistry(ALL_FACTORIES),
                new RosterPolicy(characterRepository, 10));
    }

    private void stubEmptyRosterAndSave() {
        when(characterRepository.countByOwnerId(OWNER)).thenReturn(Mono.just(0L));
        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void createsTheConcreteTypeForTheRequestedClassAndStampsTheOwner() {
        stubEmptyRosterAndSave();

        Character created = characterService.createCharacter(
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "ARMS"), OWNER).block();

        assertThat(created).isInstanceOf(Warrior.class);
        assertThat(created.getName()).isEqualTo("Conan");
        assertThat(created.getOwnerId()).isEqualTo(OWNER);
        assertThat(created.getStatValue(StatType.STRENGTH)).isEqualTo(100);
    }

    @Test
    void createsHybridClassesWithBothStats() {
        stubEmptyRosterAndSave();

        Character created = characterService.createCharacter(
                new CreateCharacterRequest("Malfurion", CharacterClass.DRUID, 200, 190, "BALANCE"), OWNER).block();

        assertThat(created).isInstanceOf(Druid.class);
        assertThat(created.getStatValue(StatType.INTELLECT)).isEqualTo(120);
        assertThat(created.getStatValue(StatType.AGILITY)).isEqualTo(50);
    }

    @Test
    void doesNotPersistAnythingWhenTheSpecializationIsInvalid() {
        when(characterRepository.countByOwnerId(OWNER)).thenReturn(Mono.just(0L));

        StepVerifier.create(characterService.createCharacter(
                        new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "NOT_A_SPEC"), OWNER))
                .expectError(BadRequestException.class)
                .verify();

        verify(characterRepository, never()).save(any(Character.class));
    }

    @Test
    void refusesToCreateBeyondTheRosterLimit() {
        when(characterRepository.countByOwnerId(OWNER)).thenReturn(Mono.just(10L));

        StepVerifier.create(characterService.createCharacter(
                        new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "ARMS"), OWNER))
                .expectError(BadRequestException.class)
                .verify();

        verify(characterRepository, never()).save(any(Character.class));
    }

    @Test
    void deleteOnlyTouchesCharactersOnTheCallersAccount() {
        Warrior conan = new Warrior("Conan", 200, 100, WarriorSpecialization.ARMS);
        conan.setId("char-1");
        conan.setOwnerId(OWNER);

        when(characterRepository.findByIdAndOwnerId("char-1", OWNER)).thenReturn(Mono.just(conan));
        when(characterRepository.deleteById("char-1")).thenReturn(Mono.empty());

        StepVerifier.create(characterService.deleteCharacter("char-1", OWNER)).verifyComplete();

        verify(characterRepository).deleteById("char-1");
    }

    /**
     * Ownership is enforced by the query, so someone else's character is simply
     * not found - there is no window between loading and checking.
     */
    @Test
    void deleteFailsForSomeoneElsesCharacterWithoutDeletingAnything() {
        when(characterRepository.findByIdAndOwnerId("char-1", OTHER_OWNER)).thenReturn(Mono.empty());

        StepVerifier.create(characterService.deleteCharacter("char-1", OTHER_OWNER))
                .expectError(NotFoundException.class)
                .verify();

        verify(characterRepository, never()).deleteById(anyString());
    }

    @Test
    void updateReassignsNothingToAnotherAccount() {
        Warrior stored = new Warrior("Conan", 200, 100, WarriorSpecialization.ARMS);
        stored.setId("char-1");
        stored.setOwnerId(OWNER);

        Warrior submitted = new Warrior("Conan the Renamed", 200, 100, WarriorSpecialization.ARMS);
        submitted.setOwnerId(OTHER_OWNER);

        when(characterRepository.findByIdAndOwnerId("char-1", OWNER)).thenReturn(Mono.just(stored));
        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Character updated = characterService.updateCharacter("char-1", OWNER, submitted).block();

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo("char-1");
        assertThat(updated.getOwnerId()).isEqualTo(OWNER);
    }
}
