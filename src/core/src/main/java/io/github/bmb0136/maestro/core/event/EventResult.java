package io.github.bmb0136.maestro.core.event;

public enum EventResult {
    OK(true),
    NOOP(true),
    UNKNOWN_TRACK,
    UNKNOWN_CLIP,
    CLIP_ALREADY_ON_TIMELINE,
    TRACK_ALREADY_ON_TIMELINE,
    INVALID_CLIP_DURATION,
    INVALID_CLIP_POSITION,
    CLIP_OVERLAP,
    WRONG_CLIP_TYPE,
    NOTE_OUTSIDE_CLIP;

    private final boolean ok;

    EventResult(boolean ok) {
        this.ok = ok;
    }

    EventResult() {
        this(false);
    }

    public boolean isOk() {
        return ok;
    }
}
