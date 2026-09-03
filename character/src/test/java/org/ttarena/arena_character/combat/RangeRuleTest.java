package org.ttarena.arena_character.combat;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.TargetType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangeRuleTest {

    private final RangeRule rule = new RangeRule();

    private static Ability reaching(int range) {
        return Ability.builder()
                .name("Lightning Bolt")
                .abilityType(AbilityType.DAMAGE)
                .targetType(TargetType.SINGLE_ENEMY)
                .range(range)
                .build();
    }

    private static CastContext at(Integer distance, int range) {
        return new CastContext(null, reaching(range), List.of("target-1"), "owner-1", distance);
    }

    @Test
    void aTargetInsideRangeIsAllowed() {
        assertThatCode(() -> rule.check(at(3, 5))).doesNotThrowAnyException();
    }

    @Test
    void aTargetExactlyAtMaximumRangeIsAllowed() {
        assertThatCode(() -> rule.check(at(5, 5))).doesNotThrowAnyException();
    }

    @Test
    void aTargetBeyondRangeIsRefused() {
        assertThatThrownBy(() -> rule.check(at(6, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("reaches 5 hexes");
    }

    @Test
    void meleeCannotReachAcrossTheBoard() {
        assertThatThrownBy(() -> rule.check(at(2, 1)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("reaches 1 hex");
    }

    @Test
    void aCallerWithoutABoardIsNotRangeChecked() {
        assertThatCode(() -> rule.check(at(null, 1))).doesNotThrowAnyException();
    }

    @Test
    void theRuleRunsAfterTheTargetsAreKnownToExist() {
        assertThatCode(() -> new TargetsRequiredRule()).doesNotThrowAnyException();
        org.assertj.core.api.Assertions.assertThat(rule.order())
                .isGreaterThan(new TargetsRequiredRule().order());
    }
}
