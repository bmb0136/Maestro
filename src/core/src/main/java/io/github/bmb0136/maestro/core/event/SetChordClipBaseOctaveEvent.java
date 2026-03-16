package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetChordClipBaseOctaveEvent extends ClipEvent {
    private final int newBaseOctave;

    public SetChordClipBaseOctaveEvent(UUID trackId, UUID clipId, int newBaseOctave) {
        super(trackId, clipId);
        this.newBaseOctave = newBaseOctave;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldBassOctave = target.getChordBuilderView().getBaseOctave();
        target.setBaseOctave(newBaseOctave);
        return oldBassOctave == newBaseOctave ? EventResult.NOOP : EventResult.OK;
    }
}
