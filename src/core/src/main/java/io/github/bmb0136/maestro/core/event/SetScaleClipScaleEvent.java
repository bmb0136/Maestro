package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import io.github.bmb0136.maestro.core.theory.ScaleType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipScaleEvent extends ClipEvent {
    private final ScaleType scale;

    public SetScaleClipScaleEvent(UUID trackId, UUID clipId, ScaleType scale) {
        super(trackId, clipId);
        this.scale = scale;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldScale = target.getScale();
        target.setScale(scale);
        return oldScale == scale ? EventResult.NOOP : EventResult.OK;
    }
}
