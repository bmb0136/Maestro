package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Timeline;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoveTrackFromTimelineEvent extends TimelineEvent {
    private final UUID trackId;

    public RemoveTrackFromTimelineEvent(UUID trackId) {
        this.trackId = trackId;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Timeline> context) {
        return context.target().removeTrack(trackId) ? EventResult.OK : EventResult.NOOP;
    }
}
