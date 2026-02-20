package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AddNoteToPianoRollClipEvent extends ClipEvent {
    private final Note note;

    protected AddNoteToPianoRollClipEvent(UUID trackId, UUID clipId, Note note) {
        super(trackId, clipId);
        this.note = note;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof PianoRollClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        if (note.position() < 0 || note.position() + note.duration() > target.getDuration() + 1e-6f) {
            return EventResult.NOTE_OUTSIDE_CLIP;
        }
        return target.addNote(note) ? EventResult.OK : EventResult.NOOP;
    }
}
