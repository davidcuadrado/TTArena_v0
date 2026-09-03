package org.ttarena.arena_map.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.model.TerrainType;

import static org.assertj.core.api.Assertions.assertThat;

class MapRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void openValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void acceptsAnOrdinaryMapName() {
        assertThat(validator.validate(new CreateMapRequest("Frozen Pass 2", "a cold place", 4))).isEmpty();
    }

    @Test
    void rejectsABlankName() {
        assertThat(validator.validate(new CreateMapRequest("   ", null, 4))).isNotEmpty();
    }

    @Test
    void rejectsANameStartingWithPunctuation() {
        assertThat(validator.validate(new CreateMapRequest("-sneaky", null, 4))).isNotEmpty();
    }

    @Test
    void rejectsANameCarryingMarkup() {
        assertThat(validator.validate(new CreateMapRequest("<script>", null, 4))).isNotEmpty();
    }

    @Test
    void acceptsAccentedLetters() {
        assertThat(validator.validate(new CreateMapRequest("Montaña Alta", null, 4))).isEmpty();
    }

    @Test
    void rejectsANegativeRadius() {
        assertThat(validator.validate(new GenerateMapRequest("Arena", null, -1, TerrainType.PLAIN))).isNotEmpty();
    }

    @Test
    void requiresATerrainWhenPlacingATile() {
        assertThat(validator.validate(new PlaceTileRequest(null, 0))).isNotEmpty();
    }

    @Test
    void rejectsAnElevationOutOfRange() {
        assertThat(validator.validate(new PlaceTileRequest(TerrainType.PLAIN, 101))).isNotEmpty();
    }

    @Test
    void rejectsANegativeArenaRadius() {
        assertThat(validator.validate(new CreateMapRequest("Frozen Pass", null, -1))).isNotEmpty();
    }

    @Test
    void acceptsASingleTileArena() {
        assertThat(validator.validate(new CreateMapRequest("Dot", null, 0))).isEmpty();
    }
}
