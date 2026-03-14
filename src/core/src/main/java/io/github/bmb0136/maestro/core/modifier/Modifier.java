package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class Modifier {
    private final UUID id;
    private boolean isMutable = false;

    public Modifier() {
        this(UUID.randomUUID());
    }

    protected Modifier(@NotNull UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public boolean isMutable() {
        return isMutable;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }

    public Modifier copy(boolean newId) {
        Modifier copy = createCopy(newId);
        assert newId == (!id.equals(copy.id));
        copy.setMutable(false);
        return copy;
    }

    protected abstract Modifier createCopy(boolean newId);

    public abstract void applyTo(@NotNull List<Note> notes);
}
