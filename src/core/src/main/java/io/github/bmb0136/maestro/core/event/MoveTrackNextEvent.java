package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Timeline;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MoveTrackNextEvent extends TimelineEvent {
    private final UUID trackId;

    public MoveTrackNextEvent(@NotNull UUID trackId) {
        this.trackId = trackId;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Timeline> context) {
        var target = context.target();
        return target.moveNext(target.indexOf(trackId)) ? EventResult.OK : EventResult.NOOP;
    }
}

