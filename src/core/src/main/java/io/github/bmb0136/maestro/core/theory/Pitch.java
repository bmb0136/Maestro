package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Pitch(@NotNull PitchName name, int octave) implements Comparable<Pitch> {
    public Pitch {
        // Have to copy logic to avoid NullPointerException
        int midi = 60 + (12 * (octave - 4)) + name.getOctaveOffset();
        if (midi < 0 || midi > 127) {
            throw new IllegalArgumentException("Pitch: outside of MIDI range (%s%d -> %d ∉ [0, 127])".formatted(name, octave, midi));
        }
    }

    public int toMidi() {
        // Middle C (C4) == 60
        return 60 + (12 * (octave() - 4)) + name().getOctaveOffset();
    }

    public static Pitch fromMidi(int midiNote, boolean useSharps) {
        int semitonesAboveMiddleC = midiNote - 60;
        int octave = Math.floorDiv(semitonesAboveMiddleC, 12) + 4;
        PitchName name = switch (Math.floorMod(semitonesAboveMiddleC, 12)) {
            case 0 -> PitchName.C;
            case 1 -> useSharps ? PitchName.C_SHARP : PitchName.D_FLAT;
            case 2 -> PitchName.D;
            case 3 -> useSharps ? PitchName.D_SHARP : PitchName.E_FLAT;
            case 4 -> PitchName.E;
            case 5 -> PitchName.F;
            case 6 -> useSharps ? PitchName.F_SHARP : PitchName.G_FLAT;
            case 7 -> PitchName.G;
            case 8 -> useSharps ? PitchName.G_SHARP : PitchName.A_FLAT;
            case 9 -> PitchName.A;
            case 10 -> useSharps ? PitchName.A_SHARP : PitchName.B_FLAT;
            case 11 -> PitchName.B;
            default -> throw new AssertionError("Unreachable");
        };
        return new Pitch(name, octave);
    }

    public Pitch addSemitones(int semitones) {
        return fromMidi(toMidi() + semitones, name().isSharp());
    }

    public Pitch addSemitones(int semitones, boolean useSharps) {
        return fromMidi(toMidi() + semitones, useSharps);
    }

    /**
     * @implNote This method can simply pass {@code rawNote} into {@link PitchName#tryParse(String)} since it ignores
     * the suffix of its input, which for valid values of {@code rawNote} would be the {@link Pitch#octave} number
     * of the {@link Pitch}.
     */
    public static Optional<Pitch> tryParse(String rawNote) {
        final String trimmed = rawNote.trim();
        return PitchName.tryParse(trimmed).flatMap(name -> {
            int nameLength = switch (name) {
                case A, B, C, D, E, F, G -> 1;
                default -> 2;
            };
            if (nameLength >= trimmed.length()) {
                return Optional.empty();
            }
            try {
                int octave = Integer.parseInt(trimmed, nameLength, trimmed.length(), 10);
                return Optional.of(new Pitch(name, octave));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public int compareTo(@NotNull Pitch other) {
        return Integer.compare(toMidi(), other.toMidi());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pitch other) {
            return compareTo(other) == 0;
        }
        return false;
    }
}
