package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.UUID;

public class RandomClip extends Clip {

    public RandomClip() {
        this(UUID.randomUUID());
    }

    protected RandomClip(UUID id) {
        super(id);
    }

    @Override
    protected Clip createCopy(boolean newId) {
        var clip = new RandomClip(newId ? UUID.randomUUID() : getId());
        return clip;
    }

    @Override
    public @NotNull Iterator<Note> iterator() {
        throw new RuntimeException("TODO");
    }
}
