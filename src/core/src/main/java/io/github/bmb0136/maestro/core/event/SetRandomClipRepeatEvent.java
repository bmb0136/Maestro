package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipRepeatEvent extends ClipEvent {
    private final boolean newRepeat;

    public SetRandomClipRepeatEvent(UUID trackId, UUID clipId, boolean newRepeat) {
        super(trackId, clipId);
        this.newRepeat = newRepeat;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRepeat = target.isRepeat();
        target.setRepeat(newRepeat);
        return oldRepeat == newRepeat ? EventResult.NOOP : EventResult.OK;
    }
}
