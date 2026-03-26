package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipMinDegreeEvent extends ClipEvent {
    private final int newMinDegree;

    public SetScaleClipMinDegreeEvent(UUID trackId, UUID clipId, int newMinDegree) {
        super(trackId, clipId);
        this.newMinDegree = newMinDegree;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMinDegree = target.getMinDegree();
        target.setMinDegree(newMinDegree);
        return oldMinDegree == newMinDegree ? EventResult.NOOP : EventResult.OK;
    }
}
