package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class Scale implements Iterable<PitchName> {
    private final PitchName keySignature;
    private final List<PitchName> pitches;

    // Note: do not call this directly, use `ScaleFactory` instead
    public Scale(PitchName keySignature, @NotNull PitchName... pitches) {
        this.keySignature = keySignature;
        this.pitches = List.of(pitches);
    }

    public PitchName getDegree(int degreeIndex) {
        return pitches.get(Math.floorMod(degreeIndex, pitches.size()));
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

    public PitchName getKeySignature() {
        return keySignature;
    }
}
