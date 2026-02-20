package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetTrackNameEvent extends TrackEvent {
    private final String name;

    public SetTrackNameEvent(UUID trackId, String name) {
        super(trackId);
        this.name = name;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Track> context) {
        if (name == null) {
            return EventResult.NOOP;
        }
        var trimmed = name.trim();
        if (trimmed.isBlank()) {
            return EventResult.NOOP;
        }
        context.target().setName(trimmed);
        return EventResult.OK;
    }
}
