package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class ChordClip extends Clip {
    private final ChordBuilder builder;

    public ChordClip() {
        super();
        builder = new ChordBuilder();
    }

    protected ChordClip(UUID id, ChordBuilder builder) {
        super(id);
        this.builder = builder;
    }

    public void setRootNote(@NotNull PitchName rootNote) {
        if (!isMutable()) {
            throw new IllegalStateException("ChordClip is immutable");
        }
        builder.setRootNote(rootNote);
    }

    public void setQuality(@NotNull ChordQuality quality) {
        if (!isMutable()) {
            throw new IllegalStateException("ChordClip is immutable");
        }
        builder.setQuality(quality);
    }

    public void setSlashNote(@Nullable PitchName slashNote) {
        if (!isMutable()) {
            throw new IllegalStateException("ChordClip is immutable");
        }
        builder.setSlashNote(slashNote);
    }

    public void setBaseOctave(int baseOctave) {
        if (!isMutable()) {
            throw new IllegalStateException("ChordClip is immutable");
        }
        builder.setBaseOctave(baseOctave);
    }

    public ChordBuilder.View getChordBuilderView() {
        return builder.getView();
    }

    @Override
    protected Clip createCopy(boolean newId) {
        return new ChordClip(newId ? UUID.randomUUID() : getId(), builder.copy());
    }

    @Override
    public @NotNull Iterator<Note> iterator() {
        ArrayList<Note> notes = new ArrayList<>();
        for (Pitch pitch : builder.build()) {
            notes.add(new Note(pitch, 0f, getDuration()));
        }
        return notes.iterator();
    }

    public static ChordClip create(float position, float duration) {
        var clip = new ChordClip();
        clip.setMutable(true);
        clip.setPosition(position);
        clip.setDuration(duration);
        clip.setMutable(false);
        return clip;
    }
}
