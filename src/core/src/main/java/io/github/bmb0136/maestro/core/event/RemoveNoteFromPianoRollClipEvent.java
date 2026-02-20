package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.theory.Pitch;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoveNoteFromPianoRollClipEvent extends ClipEvent {
    private final Pitch pitch;
    private final float position;

    protected RemoveNoteFromPianoRollClipEvent(UUID trackId, UUID clipId, Pitch pitch, float position) {
        super(trackId, clipId);
        this.pitch = pitch;
        this.position = position;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof PianoRollClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        return target.removeNote(pitch, position) ? EventResult.OK : EventResult.NOOP;
    }
}
