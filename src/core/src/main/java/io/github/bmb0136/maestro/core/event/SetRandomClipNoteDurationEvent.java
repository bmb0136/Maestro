package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipNoteDurationEvent extends ClipEvent {
    private final float newNoteDuration;

    public SetRandomClipNoteDurationEvent(UUID trackId, UUID clipId, float newNoteDuration) {
        super(trackId, clipId);
        this.newNoteDuration = newNoteDuration;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldNoteDuration = target.getNoteDuration();
        target.setNoteDuration(newNoteDuration);
        return oldNoteDuration == newNoteDuration ? EventResult.NOOP : EventResult.OK;
    }
}
