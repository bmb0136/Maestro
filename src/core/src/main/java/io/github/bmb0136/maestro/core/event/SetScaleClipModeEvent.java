package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipModeEvent extends ClipEvent {
    private final ScaleClip.Mode newMode;

    public SetScaleClipModeEvent(UUID trackId, UUID clipId, ScaleClip.Mode newMode) {
        super(trackId, clipId);
        this.newMode = newMode;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMode = target.getMode();
        target.setMode(newMode);
        return oldMode == newMode ? EventResult.NOOP : EventResult.OK;
    }
}
