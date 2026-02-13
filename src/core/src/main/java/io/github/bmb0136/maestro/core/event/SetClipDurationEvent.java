package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SetClipDurationEvent extends ClipEvent {
    private final float newDuration;

    public SetClipDurationEvent(UUID trackId, UUID clipId, float newDuration) {
        super(trackId, clipId);
        this.newDuration = newDuration;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (newDuration < 0) {
            return EventResult.INVALID_CLIP_DURATION;
        }
        Clip target = context.target();
        for (Clip other : Objects.requireNonNull(context.track())) {
            if (!other.getId().equals(target.getId())
                    && target.getPosition() < other.getPosition()
                    && other.getPosition() + newDuration > other.getPosition()) {
                return EventResult.CLIP_OVERLAP;
            }
        }
        target.setDuration(newDuration);
        return EventResult.OK;
    }
}
