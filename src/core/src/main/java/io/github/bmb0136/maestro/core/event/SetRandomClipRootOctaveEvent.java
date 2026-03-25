package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.RandomClip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetRandomClipRootOctaveEvent extends ClipEvent {
    private final int newRootOctave;

    public SetRandomClipRootOctaveEvent(UUID trackId, UUID clipId, int newRootOctave) {
        super(trackId, clipId);
        this.newRootOctave = newRootOctave;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof RandomClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRootOctave = target.getRootOctave();
        target.setRootOctave(newRootOctave);
        return oldRootOctave == newRootOctave ? EventResult.NOOP : EventResult.OK;
    }
}

