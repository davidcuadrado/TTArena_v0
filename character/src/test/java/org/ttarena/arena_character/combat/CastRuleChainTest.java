package org.ttarena.arena_character.combat;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.exception.ForbiddenException;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.TargetType;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CastRuleChainTest {

    private static final String OWNER = "owner-1";

    private final CastRuleChain chain = new CastRuleChain(List.of(
            new TargetsRequiredRule(), new ResourceCostRule(),
            new ResourceTypeRule(), new CasterOwnershipRule()));

    private Warrior conan() {
        Warrior conan = new Warrior("Conan", 200, 100, WarriorSpecialization.ARMS);
        conan.setId("caster-1");
        conan.setOwnerId(OWNER);
        return conan;
    }

    private Ability mortalStrike() {
        return Ability.builder()
                .id("ability-1")
                .name("Mortal Strike")
                .abilityType(AbilityType.DAMAGE)
                .targetType(TargetType.SINGLE_ENEMY)
                .resourceType(PowerResourceType.RAGE)
                .resourceCost(30)
                .basePower(50)
                .scalingStat(StatType.STRENGTH)
                .scalingFactor(0.5)
                .build();
    }

    @Test
    void rulesRunInDeclaredOrderRegardlessOfBeanOrder() {
        assertThat(chain.rules()).extracting(rule -> rule.getClass().getSimpleName())
                .containsExactly("CasterOwnershipRule", "ResourceTypeRule",
                        "ResourceCostRule", "TargetsRequiredRule");
    }

    @Test
    void aValidCastPassesEveryRule() {
        assertThatCode(() -> chain.check(
                new CastContext(conan(), mortalStrike(), List.of("target-1"), OWNER)))
                .doesNotThrowAnyException();
    }

    /**
     * Ownership is checked first, so a caller poking at someone else's character
     * gets 403 rather than a message leaking that character's resource state.
     */
    @Test
    void ownershipIsReportedBeforeAnyOtherProblem() {
        Warrior brokeAndNotYours = conan();
        brokeAndNotYours.setPowerResourceAmount(0);

        assertThatThrownBy(() -> chain.check(
                new CastContext(brokeAndNotYours, mortalStrike(), List.of(), "someone-else")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void anUnaffordableCastIsRejected() {
        Warrior broke = conan();
        broke.setPowerResourceAmount(5);

        assertThatThrownBy(() -> chain.check(
                new CastContext(broke, mortalStrike(), List.of("target-1"), OWNER)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not have enough");
    }

    @Test
    void aTargetedCastWithoutTargetsIsRejected() {
        assertThatThrownBy(() -> chain.check(
                new CastContext(conan(), mortalStrike(), List.of(), OWNER)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("requires at least one target");
    }
}
