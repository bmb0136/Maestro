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
                    case IONIAN, MAJOR -> fromIntervals(root, root, 2, 2, 1, 2, 2, 2);
                    case DORIAN -> fromIntervals(root, root.next(2), 2, 1, 2, 2, 2, 1);
                    case PHRYGIAN -> fromIntervals(root, root.next(4), 1, 2, 2, 2, 1, 2);
                    case LYDIAN -> fromIntervals(root, root.next(5), 2, 2, 2, 1, 2, 2);
                    case MIXOLYDIAN -> fromIntervals(root, root.next(7), 2, 2, 1, 2, 2, 1);
                    case AEOLIAN, MINOR -> fromIntervals(root, root.next(9), 2, 1, 2, 2, 1, 2);
                    case LOCRIAN -> fromIntervals(root, root.next(11), 1, 2, 2, 1, 2, 2);
                    case MAJOR_PENTATONIC -> fromIntervals(root, root, 2, 2, 3, 2);
                    case MINOR_PENTATONIC -> fromIntervals(root, root.next(9), 3, 2, 2, 3);
                    case WHOLE_HALF_DIMINISHED -> fromIntervals(root, root, 2, 1, 2, 1, 2, 1, 2);
                    case HALF_WHOLE_DIMINISHED -> fromIntervals(root, root, 1, 2, 1, 2, 1, 2, 1);
                    case CHROMATIC -> fromIntervals(root, root, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                    case WHOLE_TONE -> fromIntervals(root, root, 2, 2, 2, 2, 2);
                    case HARMONIC_MINOR -> fromIntervals(root, root, 2, 1, 2, 2, 1, 3);
                };
                CACHE.put(new Tuple2<>(type, root), scale);
            }
        }
    }

    private ScaleFactory() {
    }

    private static Scale fromIntervals(@NotNull PitchName root, @NotNull PitchName keySignature, int @NotNull ... intervals) {
        // Prefer the key signature that has fewer accidentals
        var ks1 = keySignature.convertAccidental(true);
        var ks2 = keySignature.convertAccidental(false);
        keySignature = Math.abs(ks1.getKeySignatureAccidentals()) < Math.abs(ks2.getKeySignatureAccidentals()) ? ks1 : ks2;

        var pitches = new PitchName[intervals.length + 1];
        pitches[0] = root.convertToKey(keySignature);
        for (int i = 1; i < pitches.length; i++) {
            pitches[i] = pitches[i - 1].next(intervals[i]).convertToKey(keySignature);
        }
        return new Scale(keySignature, pitches);
    }

    public static Scale create(@NotNull ScaleType type, @NotNull PitchName root) {
        return CACHE.get(new Tuple2<>(type, root));
    }
}
