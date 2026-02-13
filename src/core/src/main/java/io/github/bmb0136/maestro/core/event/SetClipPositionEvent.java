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
        for (Clip other : Objects.requireNonNull(context.track())) {
            if (other != target && newPosition + target.getDuration() > other.getPosition() && newPosition < other.getPosition() + other.getDuration()) {
                return EventResult.CLIP_OVERLAP;
            }
        }
        target.setPosition(newPosition);
        return EventResult.OK;
    }
}
