package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipMaxDegreeEvent extends ClipEvent {
    private final int newMaxDegree;

    public SetScaleClipMaxDegreeEvent(UUID trackId, UUID clipId, int newMaxDegree) {
        super(trackId, clipId);
        this.newMaxDegree = newMaxDegree;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMaxDegree = target.getMaxDegree();
        target.setMaxDegree(newMaxDegree);
        return oldMaxDegree == newMaxDegree ? EventResult.NOOP : EventResult.OK;
    }
}
