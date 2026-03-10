package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ChordQuality {
    MAJOR(4, 7),
    MINOR(3, 7),
    AUGMENTED(4, 8),
    DIMINISHED_TRIAD(3, 6),
    DIMINISHED_SEVENTH(3, 6, 9),
    HALF_DIMINISHED(3, 6, 10),
    SUS2(2, 7),
    SUS4(5, 7);

    private final List<Integer> intervalsFromRoot;

    ChordQuality(Integer... intervalsFromRoot) {
        this.intervalsFromRoot = List.of(intervalsFromRoot);
    }

    public List<Integer> getIntervalsFromRoot() {
        return intervalsFromRoot;
    }

    public PitchName getKeySignature(@NotNull PitchName rootNote) {
        return switch (this) {
            case MAJOR, AUGMENTED, SUS2, SUS4 -> rootNote;
            case MINOR, DIMINISHED_TRIAD, DIMINISHED_SEVENTH, HALF_DIMINISHED -> ScaleFactory.create(ScaleType.MINOR, rootNote).getKeySignature();
        };
    }
}
