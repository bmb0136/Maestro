package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class Scale implements Iterable<PitchName> {
    private final List<PitchName> pitches;

    public Scale(@NotNull PitchName... pitches) {
        this.pitches = List.of(pitches);
    }

    public Pitch getPitch(int degreeIndex, int baseOctave) {
        int octaveOffset = Math.floorDiv(degreeIndex, pitches.size());
        int pitchIndex = Math.floorMod(degreeIndex, pitches.size());
        return new Pitch(pitches.get(pitchIndex), baseOctave + octaveOffset);
    }

    @Override
    public @NotNull Iterator<PitchName> iterator() {
        return pitches.iterator();
    }
}
