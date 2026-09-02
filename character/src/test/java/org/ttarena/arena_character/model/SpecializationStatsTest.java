package org.ttarena.arena_character.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ttarena.arena_character.model.enums.DeathKnightSpecialization;
import org.ttarena.arena_character.model.enums.DemonHunterSpecialization;
import org.ttarena.arena_character.model.enums.DruidSpecialization;
import org.ttarena.arena_character.model.enums.EvokerSpecialization;
import org.ttarena.arena_character.model.enums.HunterSpecialization;
import org.ttarena.arena_character.model.enums.MageSpecialization;
import org.ttarena.arena_character.model.enums.MonkSpecialization;
import org.ttarena.arena_character.model.enums.PaladinSpecialization;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.RogueSpecialization;
import org.ttarena.arena_character.model.enums.ShamanSpecialization;
import org.ttarena.arena_character.model.enums.Specialization;
import org.ttarena.arena_character.model.enums.WarlockSpecialization;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SpecializationStatsTest {
    static Stream<Specialization> allSpecializations() {
        return Stream.of(
                        WarriorSpecialization.values(), PriestSpecialization.values(),
                        PaladinSpecialization.values(), RogueSpecialization.values(),
                        ShamanSpecialization.values(), HunterSpecialization.values(),
                        DeathKnightSpecialization.values(), MageSpecialization.values(),
                        WarlockSpecialization.values(), MonkSpecialization.values(),
                        DruidSpecialization.values(), DemonHunterSpecialization.values(),
                        EvokerSpecialization.values())
                .flatMap(Arrays::stream);
    }

    @ParameterizedTest
    @MethodSource("allSpecializations")
    void everySpecializationHasARoleAndBaseStats(Specialization specialization) {
        assertThat(specialization.getRole())
                .as("%s has no role", specialization.name())
                .isNotNull();

        assertThat(specialization.getBaseStats())
                .as("%s has no base stats", specialization.name())
                .isNotEmpty();

        assertThat(specialization.getBaseStats().values())
                .as("%s has a non-positive stat", specialization.name())
                .allMatch(value -> value > 0);
    }
}
