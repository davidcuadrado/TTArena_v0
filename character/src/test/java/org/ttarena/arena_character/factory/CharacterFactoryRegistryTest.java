package org.ttarena.arena_character.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.ttarena.arena_character.dto.CreateCharacterRequest;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.CharacterClass;
import org.ttarena.arena_character.model.enums.StatType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterFactoryRegistryTest {
    private static final List<CharacterFactory> ALL_FACTORIES = List.of(
            new WarriorFactory(), new PriestFactory(), new PaladinFactory(), new RogueFactory(),
            new ShamanFactory(), new HunterFactory(), new DeathKnightFactory(), new MageFactory(),
            new WarlockFactory(), new MonkFactory(), new DruidFactory(), new DemonHunterFactory(),
            new EvokerFactory());

    private final CharacterFactoryRegistry registry = new CharacterFactoryRegistry(ALL_FACTORIES);

    @ParameterizedTest
    @EnumSource(CharacterClass.class)
    void buildsACharacterForEveryClass(CharacterClass characterClass) {
        String specialization = firstSpecializationOf(characterClass);

        Character created = registry.create(
                new CreateCharacterRequest("Tester", characterClass, 100, 50, specialization));

        assertThat(created).isNotNull();
        assertThat(created.getCharacterClass()).isEqualTo(characterClass);
        assertThat(created.getName()).isEqualTo("Tester");
        assertThat(created.getHealth()).isEqualTo(100);
        assertThat(created.getPowerResourceAmount()).isEqualTo(50);
    }

    @Test
    void appliesTheSpecializationsBaseStats() {
        Character conan = registry.create(
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "ARMS"));

        assertThat(conan).isInstanceOf(Warrior.class);

        assertThat(conan.getStatValue(StatType.STRENGTH)).isEqualTo(100);

        assertThat(conan.getStatValue(StatType.SPIRIT)).isZero();
    }

    @Test
    void specializationIsCaseInsensitive() {
        Character conan = registry.create(
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "arms"));

        assertThat(conan.getStatValue(StatType.STRENGTH)).isEqualTo(100);
    }

    @Test
    void rejectsASpecializationFromAnotherClass() {
        CreateCharacterRequest request =
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "SHADOW");

        assertThatThrownBy(() -> registry.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SHADOW")
                .hasMessageContaining("ARMS");
    }

    @Test
    void rejectsABlankSpecialization() {
        CreateCharacterRequest request =
                new CreateCharacterRequest("Conan", CharacterClass.WARRIOR, 200, 100, "  ");

        assertThatThrownBy(() -> registry.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void failsFastWhenAClassHasNoFactory() {
        List<CharacterFactory> incomplete = List.of(new WarriorFactory());

        assertThatThrownBy(() -> new CharacterFactoryRegistry(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PRIEST");
    }

    @Test
    void failsFastWhenTwoFactoriesClaimTheSameClass() {
        List<CharacterFactory> duplicated = List.of(new WarriorFactory(), new WarriorFactory());

        assertThatThrownBy(() -> new CharacterFactoryRegistry(duplicated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WARRIOR");
    }

    private static String firstSpecializationOf(CharacterClass characterClass) {
        return switch (characterClass) {
            case WARRIOR -> "ARMS";
            case PRIEST -> "HOLY";
            case PALADIN -> "PROTECTION";
            case ROGUE -> "SUBTLETY";
            case SHAMAN -> "RESTORATION";
            case HUNTER -> "BEAST_MASTERY";
            case DEATH_KNIGHT -> "BLOOD";
            case MAGE -> "ARCANE";
            case WARLOCK -> "AFFLICTION";
            case MONK -> "BREWMASTER";
            case DRUID -> "BALANCE";
            case DEMON_HUNTER -> "HAVOC";
            case EVOKER -> "DEVASTATION";
        };
    }
}
