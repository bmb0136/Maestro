package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;

import java.util.UUID;

public abstract class Clip implements Iterable<Note> {
    private final UUID id;
    private float position, duration;
    private boolean mutable;

    public Clip() {
        this(UUID.randomUUID());
    }

    protected Clip(UUID id) {
        this.id = id;
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

    public UUID getId() {
        return id;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public Clip copy(boolean newId) {
        var copy = createCopy(newId);
        assert newId == (!id.equals(copy.id));
        copy.setMutable(true);
        copy.setPosition(position);
        copy.setDuration(duration);
        copy.setMutable(false);
        return copy;
    }

    protected abstract Clip createCopy(boolean newId);
}
