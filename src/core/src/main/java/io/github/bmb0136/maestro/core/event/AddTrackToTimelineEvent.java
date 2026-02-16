package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;

public class AddTrackToTimelineEvent extends TimelineEvent {
    private final Track track;

    public AddTrackToTimelineEvent(@NotNull Track track) {
        this.track = track;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Timeline> context) {
        Timeline target = context.target();
        if (target.hasTrack(track.getId())) {
            return EventResult.TRACK_ALREADY_ON_TIMELINE;
        }
        target.addTrack(track.copy(false));
        return EventResult.OK;
    }
}
