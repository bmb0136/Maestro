package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;

import java.util.UUID;

public abstract class ClipEvent extends Event<Clip> {
    private final UUID trackId, clipId;

    protected ClipEvent(UUID trackId, UUID clipId) {
        this.trackId = trackId;
        this.clipId = clipId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public UUID getClipId() {
        return clipId;
    }
}

