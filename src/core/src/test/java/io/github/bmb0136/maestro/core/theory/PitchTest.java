package io.github.bmb0136.maestro.core.theory;

import io.github.bmb0136.maestro.core.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PitchTest {
    @Test
    public void toMidi_middleCCorrect() {
        assertEquals(60, new Pitch(PitchName.C, 4).toMidi());
    }

    @Test
    public void fromMidi_middleCCorrect() {
        assertEquals(new Pitch(PitchName.C, 4), Pitch.fromMidi(60, false));
        assertEquals(new Pitch(PitchName.C, 4), Pitch.fromMidi(60, true));
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 128, 129, 130})
    public void fromMidi_outsideMidiRange_throws(int midi) {
        assertThrows(IllegalArgumentException.class, () -> Pitch.fromMidi(midi, false));
        assertThrows(IllegalArgumentException.class, () -> Pitch.fromMidi(midi, true));
    }

    @ParameterizedTest
    @MethodSource("io.github.bmb0136.maestro.core.TestUtil#allMidiNotes")
    public void fromMidi_insideMidiRange_succeeds(int midi) {
        Assertions.assertDoesNotThrow(() -> Pitch.fromMidi(midi, false));
        Assertions.assertDoesNotThrow(() -> Pitch.fromMidi(midi, true));
    }

    @ParameterizedTest
    @MethodSource("io.github.bmb0136.maestro.core.TestUtil#allMidiNotes")
    public void addSemitones_zero_doesNothing(int midi) {
        var pitch = Pitch.fromMidi(midi, false);

        var result = pitch.addSemitones(0);

        assertEquals(pitch, result);
    }

    @ParameterizedTest
    @MethodSource("io.github.bmb0136.maestro.core.TestUtil#allMidiNotes")
    public void midiConversion_alwaysEqual(int midi) {
        assertEquals(midi, Pitch.fromMidi(midi, false).toMidi());
        assertEquals(midi, Pitch.fromMidi(midi, true).toMidi());
    }

    @ParameterizedTest
    @MethodSource("tryParse_args")
    public void tryParse_basic(String input, PitchName name, int octave) {
        var result = Pitch.tryParse(input);

        assertTrue(result.isPresent());
        assertEquals(name, result.get().name());
        assertEquals(octave, result.get().octave());
    }

    private static Stream<Arguments> tryParse_args() {
        return TestUtil.allCapitalization(0, List.of(
                Arguments.of("ab4", PitchName.A_FLAT, 4),
                Arguments.of("a4", PitchName.A, 4),
                Arguments.of("a#4", PitchName.A_SHARP, 4),
                Arguments.of("bb4", PitchName.B_FLAT, 4),
                Arguments.of("b4", PitchName.B, 4),
                Arguments.of("b#4", PitchName.B_SHARP, 4),
                Arguments.of("cb4", PitchName.C_FLAT, 4),
                Arguments.of("c4", PitchName.C, 4),
                Arguments.of("c#4", PitchName.C_SHARP, 4),
                Arguments.of("db4", PitchName.D_FLAT, 4),
                Arguments.of("d4", PitchName.D, 4),
                Arguments.of("d#4", PitchName.D_SHARP, 4),
                Arguments.of("eb4", PitchName.E_FLAT, 4),
                Arguments.of("e4", PitchName.E, 4),
                Arguments.of("e#4", PitchName.E_SHARP, 4),
                Arguments.of("fb4", PitchName.F_FLAT, 4),
                Arguments.of("f4", PitchName.F, 4),
                Arguments.of("f#4", PitchName.F_SHARP, 4),
                Arguments.of("gb4", PitchName.G_FLAT, 4),
                Arguments.of("g4", PitchName.G, 4),
                Arguments.of("g#4", PitchName.G_SHARP, 4)
        ));
    }
}