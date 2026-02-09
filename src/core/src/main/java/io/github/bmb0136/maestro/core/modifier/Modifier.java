package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.Note;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Modifier {

    public abstract void applyTo(@NotNull List<Note> input);

    public Modifier copy() {
        return createCopy();
    }

    protected abstract Modifier createCopy();
}
