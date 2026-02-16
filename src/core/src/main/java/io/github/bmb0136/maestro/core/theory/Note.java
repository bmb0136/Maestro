package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Note(Pitch pitch, float position, float duration, float volume) {
    public Note {
        if (duration < 0) {
            throw new IllegalArgumentException("Note: duration must be nonnegative");
        }
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Note: volume must be a percentage (between 0 and 1)");
        }
    }

    public Note(Pitch pitch, float position, float duration) {
        this(pitch, position, duration, 1.0f);
    }

    public Note(Pitch pitch, float position) {
        this(pitch, position, 1.0f, 1.0f);
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
}
