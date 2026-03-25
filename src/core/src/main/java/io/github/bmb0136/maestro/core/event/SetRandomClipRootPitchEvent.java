package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import io.github.bmb0136.maestro.core.theory.PitchName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipRootPitchEvent extends ClipEvent {
    private final PitchName newRootPitch;

    public SetRandomClipRootPitchEvent(UUID trackId, UUID clipId, PitchName newRootPitch) {
        super(trackId, clipId);
        this.newRootPitch = newRootPitch;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRootPitch = target.getRootPitch();
        target.setRootPitch(newRootPitch);
        return oldRootPitch == newRootPitch ? EventResult.NOOP : EventResult.OK;
    }
}

