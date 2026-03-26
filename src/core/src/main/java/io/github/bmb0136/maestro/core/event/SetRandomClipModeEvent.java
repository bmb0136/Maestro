package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipModeEvent extends ClipEvent {
    private final RandomClip.Mode newMode;

    public SetRandomClipModeEvent(UUID trackId, UUID clipId, RandomClip.Mode newMode) {
        super(trackId, clipId);
        this.newMode = newMode;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldMode = target.getMode();
        target.setMode(newMode);
        return oldMode == newMode ? EventResult.NOOP : EventResult.OK;
    }
}
