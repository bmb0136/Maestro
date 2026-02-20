package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoveClipFromTrackEvent extends TrackEvent {
    private final UUID clipId;

    public RemoveClipFromTrackEvent(UUID trackId, UUID clipId) {
        super(trackId);
        this.clipId = clipId;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Track> context) {
        return context.target().removeClip(clipId) ? EventResult.OK : EventResult.NOOP;
    }
}
