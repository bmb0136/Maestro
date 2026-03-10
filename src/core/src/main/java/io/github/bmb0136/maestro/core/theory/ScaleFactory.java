package io.github.bmb0136.maestro.core.theory;

import io.github.bmb0136.maestro.core.util.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class ScaleFactory {
    // Scale objects are immutable, so we can just preload all of them
    private static final HashMap<Tuple2<ScaleType, PitchName>, Scale> CACHE = new HashMap<>();

    static {
        for (ScaleType type : ScaleType.values()) {
            for (PitchName root : PitchName.values()) {
                var scale = switch (type) {
                    case IONIAN, MAJOR -> fromIntervals(root, 2, 2, 1, 2, 2, 2);
                    case DORIAN -> fromIntervals(root, 2, 1, 2, 2, 2, 1);
                    case PHRYGIAN -> fromIntervals(root, 1, 2, 2, 2, 1, 2);
                    case LYDIAN -> fromIntervals(root, 2, 2, 2, 1, 2, 2);
                    case MIXOLYDIAN -> fromIntervals(root, 2, 2, 1, 2, 2, 1);
                    case AEOLIAN, MINOR -> fromIntervals(root, 2, 1, 2, 2, 1, 2);
                    case LOCRIAN -> fromIntervals(root, 1, 2, 2, 1, 2, 2);
                    case MAJOR_PENTATONIC -> fromIntervals(root, 2, 2, 3, 2);
                    case MINOR_PENTATONIC -> fromIntervals(root, 3, 2, 2, 3);
                    case WHOLE_HALF_DIMINISHED -> fromIntervals(root, 2, 1, 2, 1, 2, 1, 2);
                    case HALF_WHOLE_DIMINISHED -> fromIntervals(root, 1, 2, 1, 2, 1, 2, 1);
                    case CHROMATIC -> fromIntervals(root, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                    case WHOLE_TONE -> fromIntervals(root, 2, 2, 2, 2, 2);
                    case HARMONIC_MINOR -> fromIntervals(root, 2, 1, 2, 2, 1, 3);
                };
                CACHE.put(new Tuple2<>(type, root), scale);
            }
        }
    }

    private ScaleFactory() {
    }

    private static Scale fromIntervals(@NotNull PitchName root, int @NotNull ... intervals) {
        var pitches = new PitchName[intervals.length + 1];
        pitches[0] = root;
        for (int i = 1; i < pitches.length; i++) {
            pitches[i] = pitches[i - 1].next(intervals[i]).convertToKey(root);
        }
        return new Scale(pitches);
    }

    public static Scale create(@NotNull ScaleType type, @NotNull PitchName root) {
        return CACHE.get(new Tuple2<>(type, root));
    }
}
