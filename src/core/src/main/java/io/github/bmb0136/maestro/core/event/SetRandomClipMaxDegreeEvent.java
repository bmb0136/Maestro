package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipMaxDegreeEvent extends ClipEvent {
    private final int newMaxDegree;

    public SetRandomClipMaxDegreeEvent(UUID trackId, UUID clipId, int newMaxDegree) {
        super(trackId, clipId);
        this.newMaxDegree = newMaxDegree;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMaxDegree = target.getMaxDegree();
        target.setMaxDegree(newMaxDegree);
        return oldMaxDegree == newMaxDegree ? EventResult.NOOP : EventResult.OK;
    }
}
