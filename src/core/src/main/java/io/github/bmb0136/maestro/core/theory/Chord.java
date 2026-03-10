package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public final class Chord implements Iterable<Pitch> {
    private final List<Pitch> pitches;

    // Note: do not call this directly, use `ChordBuilder` instead
    public Chord(Pitch @NotNull ... pitches) {
        this.pitches = List.of(pitches);
    }

    @Override
    public @NotNull Iterator<Pitch> iterator() {
        return pitches.iterator();
    }
}
