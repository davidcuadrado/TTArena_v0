package org.ttarena.arena_map.model;

import org.junit.jupiter.api.Test;
import org.ttarena.arena_map.exception.InvalidHexCoordinateException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexCoordinateTest {

    @Test
    void rejectsCoordinatesThatDoNotSumToZero() {
        assertThatThrownBy(() -> new HexCoordinate(1, 1, 1))
                .isInstanceOf(InvalidHexCoordinateException.class);
    }

    @Test
    void derivesTheThirdAxisFromTheOtherTwo() {
        assertThat(HexCoordinate.axial(2, -3)).isEqualTo(new HexCoordinate(2, -3, 1));
    }

    @Test
    void distanceIsZeroToItself() {
        assertThat(HexCoordinate.origin().distanceTo(HexCoordinate.origin())).isZero();
    }

    @Test
    void everyNeighbourIsOneStepAway() {
        HexCoordinate centre = HexCoordinate.axial(3, -1);
        List<HexCoordinate> neighbours = centre.neighbours();

        assertThat(neighbours).hasSize(6).doesNotHaveDuplicates();
        assertThat(neighbours).allSatisfy(neighbour ->
                assertThat(centre.distanceTo(neighbour)).isEqualTo(1));
    }

    @Test
    void keyRoundTripsThroughParse() {
        HexCoordinate coordinate = HexCoordinate.axial(-4, 7);
        assertThat(HexCoordinate.parse(coordinate.key())).isEqualTo(coordinate);
    }

    @Test
    void parseRejectsMalformedKeys() {
        assertThatThrownBy(() -> HexCoordinate.parse("1:2"))
                .isInstanceOf(InvalidHexCoordinateException.class);
        assertThatThrownBy(() -> HexCoordinate.parse("a:b:c"))
                .isInstanceOf(InvalidHexCoordinateException.class);
        assertThatThrownBy(() -> HexCoordinate.parse(null))
                .isInstanceOf(InvalidHexCoordinateException.class);
    }

    @Test
    void ringIndexGrowsWithDistanceFromTheOrigin() {
        assertThat(HexCoordinate.origin().ringIndex()).isZero();
        assertThat(HexCoordinate.axial(2, -1).ringIndex()).isEqualTo(2);
    }
}
