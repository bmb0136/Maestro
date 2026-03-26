package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipRepeatEvent extends ClipEvent {
    private final boolean newRepeat;

    public SetScaleClipRepeatEvent(UUID trackId, UUID clipId, boolean newRepeat) {
        super(trackId, clipId);
        this.newRepeat = newRepeat;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRepeat = target.isRepeat();
        target.setRepeat(newRepeat);
        return oldRepeat == newRepeat ? EventResult.NOOP : EventResult.OK;
    }
}
