package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.modifier.ModifierList;
import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Clip implements Iterable<Note> {
    @NotNull
    private final UUID id;
    private float position, duration;
    private boolean mutable;
    @NotNull
    private final ModifierList modifiers = new ModifierList(this::isMutable);

    public Clip() {
        this(UUID.randomUUID());
    }

    protected Clip(@NotNull UUID id) {
        this.id = id;
    }

    public @NotNull ModifierList getModifiers() {
        return modifiers;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        if (!mutable) {
            throw new IllegalStateException("Clip is immutable");
        }
        this.position = position;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        if (!mutable) {
            throw new IllegalStateException("Clip is immutable");
        }
        this.duration = duration;
    }

    public @NotNull UUID getId() {
        return id;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public Clip copyWithPosition(float newPosition) {
        var clip = copy(true);
        clip.setMutable(true);
        clip.setPosition(newPosition);
        clip.setMutable(false);
        return clip;
    }

    public Clip copy(boolean newId) {
        var copy = createCopy(newId);
        assert newId == (!id.equals(copy.id));
        copy.setMutable(true);
        copy.setPosition(position);
        copy.setDuration(duration);
        for (Modifier m : modifiers) {
            copy.modifiers.addModifier(m);
        }
        copy.setMutable(false);
        return copy;
    }

    protected abstract Clip createCopy(boolean newId);
}
