package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class AddClipToTrackEvent extends TrackEvent {
    private final Clip clip;

    public AddClipToTrackEvent(UUID trackId, @NotNull Clip clip) {
        super(trackId);
        this.clip = clip;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Track> context) {
        Track target = context.target();
        for (Track track : Objects.requireNonNull(context.timeline())) {
            if (track.hasClip(clip.getId())) {
                return EventResult.CLIP_ALREADY_ON_TIMELINE;
            }
        }
        if (CommonChecks.doesClipOverlapExisting(target, clip)) {
            return EventResult.CLIP_OVERLAP;
        }
        target.addClip(clip.copy(false));
        return EventResult.OK;
    }
}
