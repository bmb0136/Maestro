package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import io.github.bmb0136.maestro.core.theory.ScaleType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipScaleEvent extends ClipEvent {
    private final ScaleType scale;

    public SetRandomClipScaleEvent(UUID trackId, UUID clipId, ScaleType scale) {
        super(trackId, clipId);
        this.scale = scale;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldScale = target.getScale();
        target.setScale(scale);
        return oldScale == scale ? EventResult.NOOP : EventResult.OK;
    }
}
