package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.Note;

public abstract class Clip implements Iterable<Note> {
    private float position, duration;

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
        onPositionChanged();
    }

    public Clip copy() {
        var c = createCopy();
        c.setPosition(position);
        return c;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Clip duration must be positive");
        }
        this.duration = duration;
        onDurationChanged();
    }

    protected abstract Clip createCopy();

    protected void onPositionChanged() {
    }
    protected void onDurationChanged() {
    }

}
