package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.Note;
import io.github.bmb0136.maestro.core.Pitch;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class PianoRollClip extends Clip {
    private final TreeSet<Note> notes = new TreeSet<>();

    public void addNote(@NotNull Note note) {
        if (note.position() < 0 || note.position() + note.duration() > getDuration()) {
            throw new IllegalArgumentException("Note cannot be placed outside of piano roll clip");
        }
        notes.add(note);
    }

    public boolean removeNote(@NotNull Note note) {
        return notes.remove(note);
    }

    /**
     * Delete a note that is "under the cursor" given by {@code cursorPitch} and {@code cursorPosition}.
     *
     * @return {@code true} if a note was removed, or {@code false} otherwise
     */
    public boolean removeNoteAtCursor(@NotNull Pitch cursorPitch, float cursorPosition) {
        Note toRemove = null;
        for (Note note : notes) {
            if (!note.pitch().equals(cursorPitch)) {
                continue;
            }
            if (cursorPosition < note.position()) {
                continue;
            }
            if (cursorPosition > note.position() + note.duration()) {
                continue;
            }
            toRemove = note;
            break;
        }
        if (toRemove != null) {
            notes.remove(toRemove);
        }
        return toRemove != null;
    }

    // TODO: individual and bulk note manipulation methods

    @Override
    public @NotNull Iterator<Note> iterator() {
        return notes.iterator();
    }

    @Override
    protected Clip createCopy() {
        PianoRollClip clip = new PianoRollClip();
        clip.notes.addAll(notes);
        return clip;
    }

    @Override
    protected void onDurationChanged() {
        ArrayList<Note> toRemove = new ArrayList<>();
        for (Note note : notes) {
            if (note.position() + note.duration() > getDuration()) {
                toRemove.add(note);
            }
        }
        toRemove.forEach(notes::remove);
    }
}
