package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipMinDegreeEvent extends ClipEvent {
    private final int newMinDegree;

    public SetRandomClipMinDegreeEvent(UUID trackId, UUID clipId, int newMinDegree) {
        super(trackId, clipId);
        this.newMinDegree = newMinDegree;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMinDegree = target.getMinDegree();
        target.setMinDegree(newMinDegree);
        return oldMinDegree == newMinDegree ? EventResult.NOOP : EventResult.OK;
    }
}
