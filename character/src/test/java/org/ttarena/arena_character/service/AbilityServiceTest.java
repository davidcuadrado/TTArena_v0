package org.ttarena.arena_character.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ttarena.arena_character.combat.AbilityEffectRegistry;
import org.ttarena.arena_character.combat.CastRuleChain;
import org.ttarena.arena_character.combat.CasterOwnershipRule;
import org.ttarena.arena_character.combat.DamageEffect;
import org.ttarena.arena_character.combat.HealEffect;
import org.ttarena.arena_character.combat.ResourceCostRule;
import org.ttarena.arena_character.combat.ResourceTypeRule;
import org.ttarena.arena_character.combat.TargetsRequiredRule;
import org.ttarena.arena_character.exception.ForbiddenException;
import org.ttarena.arena_character.exception.BadRequestException;
import org.ttarena.arena_character.model.Ability;
import org.ttarena.arena_character.model.Character;
import org.ttarena.arena_character.model.CombatResult;
import org.ttarena.arena_character.model.Priest;
import org.ttarena.arena_character.model.TargetOutcome;
import org.ttarena.arena_character.model.Warrior;
import org.ttarena.arena_character.model.enums.AbilityType;
import org.ttarena.arena_character.model.enums.PowerResourceType;
import org.ttarena.arena_character.model.enums.PriestSpecialization;
import org.ttarena.arena_character.model.enums.StatType;
import org.ttarena.arena_character.model.enums.TargetType;
import org.ttarena.arena_character.model.enums.WarriorSpecialization;
import org.ttarena.arena_character.repository.AbilityRepository;
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
class AbilityServiceTest {

    private static final String OWNER = "owner-1";
    private static final String OTHER_OWNER = "owner-2";
    @Mock
    private AbilityRepository abilityRepository;

    @Mock
    private CharacterRepository characterRepository;

    private AbilityService abilityService;

    private Warrior conan;
    private Priest anduin;

    @BeforeEach
    void setUp() {
        abilityService = new AbilityService(abilityRepository, characterRepository,
                new AbilityEffectRegistry(List.of(new DamageEffect(), new HealEffect())),
                new CastRuleChain(List.of(new CasterOwnershipRule(), new ResourceTypeRule(),
                        new ResourceCostRule(), new TargetsRequiredRule())));

        conan = new Warrior("Conan", 200, 100, WarriorSpecialization.ARMS);
        conan.setId("caster-1");
        conan.setOwnerId(OWNER);

        anduin = new Priest("Anduin", 200, 100, PriestSpecialization.HOLY);
        anduin.setId("target-1");
        anduin.setOwnerId(OTHER_OWNER);
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

    private void stubSaves() {
        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void castSpendsResourceScalesOffTheCastersStatAndMitigatesWithArmor() {
        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(characterRepository.findById("target-1")).thenReturn(Mono.just(anduin));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));
        stubSaves();

        CombatResult result = abilityService
                .castAbility("caster-1", "ability-1", List.of("target-1"), OWNER, null)
                .block();

        assertThat(result).isNotNull();
        assertThat(result.getCasterName()).isEqualTo("Conan");
        assertThat(result.getResourceSpent()).isEqualTo(30);
        assertThat(result.getCasterRemainingResource()).isEqualTo(70);

        assertThat(result.getOutcomes()).hasSize(1);
        TargetOutcome outcome = result.getOutcomes().get(0);
        assertThat(outcome.getTargetName()).isEqualTo("Anduin");
        assertThat(outcome.getAmount()).isEqualTo(89);
        assertThat(outcome.getResultingHealth()).isEqualTo(111);
        assertThat(outcome.isDefeated()).isFalse();
    }

    @Test
    void aSelfTargetedHealIgnoresTheSuppliedTargetsAndHealsTheCaster() {
        anduin.applyDamage(100);
        Ability renew = Ability.builder()
                .id("ability-2")
                .name("Renew")
                .abilityType(AbilityType.HEAL)
                .targetType(TargetType.SELF)
                .resourceType(PowerResourceType.MANA)
                .resourceCost(20)
                .basePower(10)
                .scalingStat(StatType.INTELLECT)
                .scalingFactor(0.5)
                .build();

        when(characterRepository.findById("target-1")).thenReturn(Mono.just(anduin));
        when(abilityRepository.findById("ability-2")).thenReturn(Mono.just(renew));
        stubSaves();

        CombatResult result = abilityService
                .castAbility("target-1", "ability-2", List.of("someone-else"), OTHER_OWNER, null)
                .block();

        assertThat(result).isNotNull();

        assertThat(result.getOutcomes()).hasSize(1);
        assertThat(result.getOutcomes().get(0).getTargetName()).isEqualTo("Anduin");
        assertThat(result.getOutcomes().get(0).getAmount()).isEqualTo(55);
        assertThat(anduin.getHealth()).isEqualTo(155);
    }

    @Test
    void aBuffResolvesToNoEffectRatherThanFailing() {
        Ability battleShout = Ability.builder()
                .id("ability-3")
                .name("Battle Shout")
                .abilityType(AbilityType.BUFF)
                .targetType(TargetType.SELF)
                .resourceType(PowerResourceType.RAGE)
                .resourceCost(10)
                .basePower(100)
                .scalingStat(StatType.STRENGTH)
                .scalingFactor(1.0)
                .build();

        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-3")).thenReturn(Mono.just(battleShout));
        stubSaves();

        CombatResult result = abilityService
                .castAbility("caster-1", "ability-3", List.of(), OWNER, null)
                .block();

        assertThat(result).isNotNull();

        assertThat(result.getCasterRemainingResource()).isEqualTo(90);
        assertThat(result.getOutcomes().get(0).getAmount()).isZero();
        assertThat(conan.getHealth()).isEqualTo(200);
    }

    @Test
    void refusesToCastAsACharacterOnAnotherAccount() {
        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));

        StepVerifier.create(abilityService.castAbility("caster-1", "ability-1", List.of("target-1"), OTHER_OWNER, null))
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    void ownershipIsCheckedBeforeTheResourceIsSpent() {
        conan.setPowerResourceAmount(100);

        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));

        StepVerifier.create(abilityService.castAbility("caster-1", "ability-1", List.of("target-1"), OTHER_OWNER, null))
                .expectError(ForbiddenException.class)
                .verify();

        assertThat(conan.getPowerResourceAmount()).isEqualTo(100);
        verify(characterRepository, never()).save(any(Character.class));
    }

    @Test
    void aTargetOnAnotherAccountIsFineToAttack() {
        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(characterRepository.findById("target-1")).thenReturn(Mono.just(anduin));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));
        stubSaves();

        CombatResult result = abilityService
                .castAbility("caster-1", "ability-1", List.of("target-1"), OWNER, null)
                .block();

        assertThat(result).isNotNull();
        assertThat(anduin.getOwnerId()).isNotEqualTo(conan.getOwnerId());
        assertThat(result.getOutcomes()).hasSize(1);
    }

    @Test
    void rejectsAnAbilityThatCostsADifferentResourceThanTheCasterUses() {
        Ability manaAbility = Ability.builder()
                .id("ability-1")
                .name("Mortal Strike")
                .abilityType(AbilityType.DAMAGE)
                .targetType(TargetType.SINGLE_ENEMY)
                .resourceType(PowerResourceType.MANA)
                .resourceCost(30)
                .basePower(50)
                .scalingStat(StatType.STRENGTH)
                .scalingFactor(0.5)
                .build();

        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(manaAbility));

        StepVerifier.create(abilityService.castAbility("caster-1", "ability-1", List.of("target-1"), OWNER, null))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void rejectsACastTheCasterCannotAfford() {
        conan.setPowerResourceAmount(5);

        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));

        StepVerifier.create(abilityService.castAbility("caster-1", "ability-1", List.of("target-1"), OWNER, null))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void rejectsATargetedCastWithNoTargets() {
        when(characterRepository.findById("caster-1")).thenReturn(Mono.just(conan));
        when(abilityRepository.findById("ability-1")).thenReturn(Mono.just(mortalStrike()));

        StepVerifier.create(abilityService.castAbility("caster-1", "ability-1", List.of(), OWNER, null))
                .expectError(BadRequestException.class)
                .verify();
    }
}
