package io.github.bmb0136.maestro.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Note(Pitch pitch, float position, float duration, float volume) implements Comparable<Note> {
    public Note {
        if (duration < 0) {
            throw new IllegalArgumentException("Note: duration must be nonnegative");
        }
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Note: volume must be a percentage (between 0 and 1)");
        }
    }

    /**
     * Modify the {@link Note#pitch()} of this {@link Note} by applying the provided {@link Function}
     *
     * @return The modified {@link Note}
     */
    public Note modifyPitch(@NotNull Function<Pitch, Pitch> f) {
        return new Note(f.apply(pitch), position, duration, volume);
    }

    /**
     * Modify the {@link Note#position()} of this {@link Note} by applying the provided {@link Function}
     *
     * @return The modified {@link Note}
     */
    public Note modifyPosition(@NotNull Function<Float, Float> f) {
        return new Note(pitch, f.apply(position), duration, volume);
    }

    /**
     * Modify the {@link Note#duration()} of this {@link Note} by applying the provided {@link Function}
     *
     * @return The modified {@link Note}
     */
    public Note modifyDuration(@NotNull Function<Float, Float> f) {
        return new Note(pitch, position, f.apply(duration), volume);
    }

    /**
     * Modify the {@link Note#volume()} of this {@link Note} by applying the provided {@link Function}
     *
     * @return The modified {@link Note}
     */
    public Note modifyVolume(@NotNull Function<Float, Float> f) {
        return new Note(pitch, position, duration, f.apply(volume));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Note) obj;
        return compareTo(that) == 0;
    }

    private static int floatCompare(float a, float b) {
        float d = a - b;
        if (Math.abs(d) < 1e-6) {
            return 0;
        }
        return d < 0 ? -1 : 1;
    }

    @Override
    public int compareTo(@NotNull Note other) {
        int cmpPos = floatCompare(position, other.position);
        if (cmpPos != 0) {
            return cmpPos;
        }
        int cmpPitch = pitch.compareTo(other.pitch);
        if (cmpPitch != 0) {
            return cmpPitch;
        }
        int cmpDur = floatCompare(duration, other.duration);
        if (cmpDur != 0) {
            return cmpDur;
        }
        return floatCompare(volume, other.volume);
    }
}
