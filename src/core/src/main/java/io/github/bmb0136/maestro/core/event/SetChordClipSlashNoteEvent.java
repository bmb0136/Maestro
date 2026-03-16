package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.PitchName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SetChordClipSlashNoteEvent extends ClipEvent {
    private final PitchName newSlashNote;

    public SetChordClipSlashNoteEvent(UUID trackId, UUID clipId, @Nullable PitchName newSlashNote) {
        super(trackId, clipId);
        this.newSlashNote = newSlashNote;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldSlashNote = target.getChordBuilderView().getSlashNote();
        target.setSlashNote(newSlashNote);
        return oldSlashNote == newSlashNote ? EventResult.NOOP : EventResult.OK;
    }
}
