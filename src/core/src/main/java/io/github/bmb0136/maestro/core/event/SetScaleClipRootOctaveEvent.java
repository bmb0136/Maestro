package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetScaleClipRootOctaveEvent extends ClipEvent {
    private final int newRootOctave;

    public SetScaleClipRootOctaveEvent(UUID trackId, UUID clipId, int newRootOctave) {
        super(trackId, clipId);
        this.newRootOctave = newRootOctave;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ScaleClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRootOctave = target.getRootOctave();
        target.setRootOctave(newRootOctave);
        return oldRootOctave == newRootOctave ? EventResult.NOOP : EventResult.OK;
    }
}

