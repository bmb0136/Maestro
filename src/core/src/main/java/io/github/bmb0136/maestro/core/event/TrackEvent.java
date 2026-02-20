package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Track;

import java.util.UUID;

public abstract class TrackEvent extends Event<Track> {
    private final UUID trackId;

    protected TrackEvent(UUID trackId) {
        this.trackId = trackId;
    }

    public UUID getTrackId() {
        return trackId;
    }
}
