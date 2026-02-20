package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SetClipPositionEvent extends ClipEvent {
    private final float newPosition;

    public SetClipPositionEvent(UUID trackId, UUID clipId, float newPosition) {
        super(trackId, clipId);
        this.newPosition = newPosition;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (newPosition < 0) {
            return EventResult.INVALID_CLIP_POSITION;
        }
        Clip target = context.target();
        target.setPosition(newPosition);
        if (CommonEventChecks.doesClipOverlapExisting(Objects.requireNonNull(context.track()), target)) {
            return EventResult.CLIP_OVERLAP;
        }
        return EventResult.OK;
    }
}
