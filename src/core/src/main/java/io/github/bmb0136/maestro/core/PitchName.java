package io.github.bmb0136.maestro.core;

import java.util.Optional;

public enum PitchName {
    A_FLAT,
    A,
    A_SHARP,
    B_FLAT,
    B,
    B_SHARP,
    C_FLAT,
    C,
    C_SHARP,
    D_FLAT,
    D,
    D_SHARP,
    E_FLAT,
    E,
    E_SHARP,
    F_FLAT,
    F,
    F_SHARP,
    G_FLAT,
    G,
    G_SHARP;

    public PitchName next() {
        return switch (this) {
            case A_FLAT, G_SHARP -> A;
            case A -> A_SHARP;
            case A_SHARP, B_FLAT -> B;
            case B, C_FLAT -> C;
            case B_SHARP, C -> C_SHARP;
            case C_SHARP, D_FLAT -> D;
            case D -> D_SHARP;
            case D_SHARP, E_FLAT -> E;
            case E, F_FLAT -> F;
            case E_SHARP, F -> F_SHARP;
            case F_SHARP, G_FLAT -> G;
            case G -> G_SHARP;
        };
    }

    public PitchName next(int semitones) {
        PitchName n = this;
        for (int i = 0; i < semitones; i++) {
            n = this.next();
        }
        return n;
    }

    public PitchName previous() {
        return switch (this) {
            case A_FLAT, G_SHARP -> G;
            case A -> A_FLAT;
            case A_SHARP, B_FLAT -> A;
            case B, C_FLAT -> B_FLAT;
            case B_SHARP, C -> B;
            case C_SHARP, D_FLAT -> C;
            case D -> D_FLAT;
            case D_SHARP, E_FLAT -> D;
            case E, F_FLAT -> E_FLAT;
            case E_SHARP, F -> E;
            case F_SHARP, G_FLAT -> F;
            case G -> G_FLAT;
        };
    }

    public PitchName previous(int semitones) {
        PitchName n = this;
        for (int i = 0; i < semitones; i++) {
            n = n.previous();
        }
        return n;
    }

    public boolean isSharp() {
        return switch (this) {
            case A_SHARP, B_SHARP, C_SHARP, D_SHARP, E_SHARP, F_SHARP, G_SHARP -> true;
            default -> false;
        };
    }

    public int getOctaveOffset() {
        return switch (this) {
            case B_SHARP, C -> 0;
            case C_SHARP, D_FLAT -> 1;
            case D -> 2;
            case D_SHARP, E_FLAT -> 3;
            case E, F_FLAT -> 4;
            case E_SHARP, F -> 5;
            case F_SHARP, G_FLAT -> 6;
            case G -> 7;
            case A_FLAT, G_SHARP -> 8;
            case A -> 9;
            case A_SHARP, B_FLAT -> 10;
            case B, C_FLAT -> 11;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case A_FLAT -> "Ab";
            case A -> "A";
            case A_SHARP -> "A#";
            case B_FLAT -> "Bb";
            case B -> "B";
            case B_SHARP -> "B#";
            case C_FLAT -> "Cb";
            case C -> "C";
            case C_SHARP -> "C#";
            case D_FLAT -> "Db";
            case D -> "D";
            case D_SHARP -> "D#";
            case E_FLAT -> "Eb";
            case E -> "E";
            case E_SHARP -> "E#";
            case F_FLAT -> "Fb";
            case F -> "F";
            case F_SHARP -> "F#";
            case G_FLAT -> "Gb";
            case G -> "G";
            case G_SHARP -> "G#";
        };
    }

    /**
     * @implNote This method only considers up to the first two characters of {@code rawName}.
     * This is done to allow {@link Pitch#tryParse(String)} to simply pass its input into this method to obtain a {@link PitchName}
     */
    public static Optional<PitchName> tryParse(String rawName) {
        rawName = rawName.trim();
        if (rawName.isEmpty()) {
            return Optional.empty();
        }

        // Check first character
        PitchName base;
        switch (Character.toLowerCase(rawName.charAt(0))) {
            case 'a' -> base = A;
            case 'b' -> base = B;
            case 'c' -> base = C;
            case 'd' -> base = D;
            case 'e' -> base = E;
            case 'f' -> base = F;
            case 'g' -> base = G;
            default -> {
                return Optional.empty();
            }
        }

        // Check for accidental
        if (rawName.length() < 2) {
            return Optional.of(base);
        }
        return switch (rawName.charAt(1)) {
            case 'b', 'B' -> Optional.ofNullable(base.previous());
            case '#' -> Optional.ofNullable(base.next());
            default -> Optional.of(base);
        };
    }
}

