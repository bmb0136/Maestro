package io.github.bmb0136.maestro.core.theory;

import io.github.bmb0136.maestro.core.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PitchNameTest {
    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void isAccidental_mutuallyExclusive(PitchName input) {
        var sharp = input.isSharp() ? 1 : 0;
        var flat = input.isFlat() ? 1 : 0;
        var natural = input.isNatural() ? 1 : 0;
        assertEquals(1, sharp + flat + natural);
    }

    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void next_alwaysSharpOrNatural(PitchName input) {
        var result = input.next();
        assertTrue(result.isSharp() || result.isNatural());
    }

    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void next_negativeSemitones_equivalentToPrevious(PitchName input) {
        final int OFFSET = 3;
        assertEquals(input.previous(OFFSET), input.next(-OFFSET));
    }

    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void previous_alwaysFlatOrNatural(PitchName input) {
        var result = input.previous();
        assertTrue(result.isFlat() || result.isNatural());
    }

    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void previous_negativeSemitones_equivalentToNext(PitchName input) {
        final int OFFSET = 3;
        assertEquals(input.next(OFFSET), input.previous(-OFFSET));
    }

    @ParameterizedTest
    @EnumSource(PitchName.class)
    public void toString_correctAccidentals(PitchName input) {
        var result = input.toString();

        var hasSharp = result.endsWith("#");
        var hasFlat = result.endsWith("b");
        assertEquals(input.isSharp(), hasSharp);
        assertEquals(input.isFlat(), hasFlat);
        assertEquals(input.isNatural(), !(hasSharp || hasFlat));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "   \n  "})
    public void tryParse_emptyInput_fails(String input) {
        assertTrue(PitchName.tryParse(input).isEmpty());
    }

    @Test
    public void tryParse_nullInput_fails() {
        assertTrue(PitchName.tryParse(null).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("tryParse_args")
    public void tryParse_valid(String input, PitchName expected) {
        var result = PitchName.tryParse(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    private static Stream<Arguments> tryParse_args() {
        return TestUtil.allCapitalization(0, List.of(
                Arguments.of("ab", PitchName.A_FLAT),
                Arguments.of("a", PitchName.A),
                Arguments.of("a#", PitchName.A_SHARP),
                Arguments.of("bb", PitchName.B_FLAT),
                Arguments.of("b", PitchName.B),
                Arguments.of("b#", PitchName.B_SHARP),
                Arguments.of("cb", PitchName.C_FLAT),
                Arguments.of("c", PitchName.C),
                Arguments.of("c#", PitchName.C_SHARP),
                Arguments.of("db", PitchName.D_FLAT),
                Arguments.of("d", PitchName.D),
                Arguments.of("d#", PitchName.D_SHARP),
                Arguments.of("eb", PitchName.E_FLAT),
                Arguments.of("e", PitchName.E),
                Arguments.of("e#", PitchName.E_SHARP),
                Arguments.of("fb", PitchName.F_FLAT),
                Arguments.of("f", PitchName.F),
                Arguments.of("f#", PitchName.F_SHARP),
                Arguments.of("gb", PitchName.G_FLAT),
                Arguments.of("g", PitchName.G),
                Arguments.of("g#", PitchName.G_SHARP)
        ));
    }
}