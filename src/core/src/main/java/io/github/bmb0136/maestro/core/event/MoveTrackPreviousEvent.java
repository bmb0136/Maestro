package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Timeline;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MoveTrackPreviousEvent extends TimelineEvent {
    private final UUID trackId;

    public MoveTrackPreviousEvent(@NotNull UUID trackId) {
        this.trackId = trackId;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Timeline> context) {
        var target = context.target();
        return target.movePrevious(target.indexOf(trackId)) ? EventResult.OK : EventResult.NOOP;
    }
}
