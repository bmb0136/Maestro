package io.github.bmb0136.maestro.core;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

public class TestClip extends Clip {
    public TestClip() {
        super();
    }

    protected TestClip(UUID id) {
        super(id);
    }

    @Override
    protected Clip createCopy(boolean newId) {
        return new TestClip(newId ? UUID.randomUUID() : getId());
    }

    @Override
    public @NotNull Iterator<Note> iterator() {
        return Collections.emptyIterator();
    }
}
