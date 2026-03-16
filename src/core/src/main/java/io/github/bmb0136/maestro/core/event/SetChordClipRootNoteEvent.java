package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.PitchName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetChordClipRootNoteEvent extends ClipEvent {
    @NotNull
    private final PitchName newRootNote;

    public SetChordClipRootNoteEvent(UUID trackId, UUID clipId, @NotNull PitchName newRootNote) {
        super(trackId, clipId);
        this.newRootNote = newRootNote;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldRootNote = target.getChordBuilderView().getRootNote();
        target.setRootNote(newRootNote);
        return oldRootNote == newRootNote ? EventResult.NOOP : EventResult.OK;
    }
}
