package org.ttarena.arena_character.combat;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbilityEffectTest {

    private final DamageEffect damage = new DamageEffect();
    private final HealEffect heal = new HealEffect();

    /** Plate armor is 200: 200 / (200 + 400) = 33.3% mitigated, so 100 lands as 67. */
    @Test
    void plateArmorMitigatesAThird() {
        Character muradin = new Warrior("Muradin", 200, 100, WarriorSpecialization.PROTECTION);

        int dealt = damage.apply(muradin, 100);

        assertThat(dealt).isEqualTo(67);
        assertThat(muradin.getHealth()).isEqualTo(133);
    }

    /** Cloth armor is 50: 50 / (50 + 400) = 11.1% mitigated, so 100 lands as 89. */
    @Test
    void clothArmorBarelyMitigates() {
        Character anduin = new Priest("Anduin", 200, 100, PriestSpecialization.HOLY);

        int dealt = damage.apply(anduin, 100);

        assertThat(dealt).isEqualTo(89);
        assertThat(anduin.getHealth()).isEqualTo(111);
    }

    @Test
    void mitigationIsCappedSoAHitAlwaysLands() {
        Character muradin = new Warrior("Muradin", 200, 100, WarriorSpecialization.PROTECTION);
        muradin.setArmor(1_000_000);

        // Capped at 75% reduction rather than scaling to immunity.
        assertThat(damage.apply(muradin, 100)).isEqualTo(25);
        // And even a 0-power hit does its floor of 1.
        assertThat(damage.apply(muradin, 0)).isEqualTo(1);
    }

    @Test
    void damageNeverOverkillsPastZero() {
        Character muradin = new Warrior("Muradin", 10, 100, WarriorSpecialization.PROTECTION);

        int dealt = damage.apply(muradin, 1000);

        assertThat(dealt).isEqualTo(10);
        assertThat(muradin.getHealth()).isZero();
        assertThat(muradin.isAlive()).isFalse();
    }

    @Test
    void healingIsClampedAtMaxHealth() {
        Character anduin = new Priest("Anduin", 200, 100, PriestSpecialization.HOLY);
        anduin.applyDamage(100);

        assertThat(heal.apply(anduin, 50)).isEqualTo(50);
        assertThat(anduin.getHealth()).isEqualTo(150);

        // Only the missing 50 counts as healed.
        assertThat(heal.apply(anduin, 500)).isEqualTo(50);
        assertThat(anduin.getHealth()).isEqualTo(200);
    }

    @Test
    void registryResolvesEffectsByTypeAndReportsGaps() {
        AbilityEffectRegistry registry = new AbilityEffectRegistry(List.of(damage, heal));

        assertThat(registry.forType(AbilityType.DAMAGE)).containsSame(damage);
        assertThat(registry.forType(AbilityType.HEAL)).containsSame(heal);
        // BUFF and DEBUFF are deliberately unimplemented, and must stay resolvable-to-empty
        // rather than blowing up a cast.
        assertThat(registry.forType(AbilityType.BUFF)).isEmpty();
        assertThat(registry.forType(AbilityType.DEBUFF)).isEmpty();
    }
}
