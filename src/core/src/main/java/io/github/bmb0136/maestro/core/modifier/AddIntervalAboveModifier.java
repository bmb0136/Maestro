package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AddIntervalAboveModifier extends Modifier {

    private int semitones = 0;

    public AddIntervalAboveModifier() {
        super();
    }

    protected AddIntervalAboveModifier(UUID id) {
        super(id);
    }

    @Override
    protected Modifier createCopy(boolean newId) {
        var copy = new AddIntervalAboveModifier(newId ? UUID.randomUUID() : getId());
        copy.semitones = semitones;
        return copy;
    }

    @Override
    public void applyTo(@NotNull List<Note> notes) {
        if (semitones == 0) {
            return;
        }

        ArrayList<Note> topNotes = new ArrayList<>();

        // Find the highest notes for every distinct position
        for (Note note : notes) {
            boolean anyWithEqualPosition = false;
            for (int i = 0; i < topNotes.size(); i++) {
                Note currentTop = topNotes.get(i);

                if (Math.abs(note.position() - currentTop.position()) > 1e-6f) {
                    continue;
                }

                anyWithEqualPosition = true;

                // Replace if note is above
                if (note.pitch().compareTo(currentTop.pitch()) > 0) {
                    topNotes.set(i, note);
                }

                // There can only be one Note in topNotes per position, so no need to look further
                break;
            }

            if (!anyWithEqualPosition) {
                topNotes.add(note);
            }
        }

        for (Note top : topNotes) {
            notes.add(top.modifyPitch(p -> p.addSemitones(semitones)));
        }
    }

    public int getSemitones() {
        return semitones;
    }

    public boolean setSemitones(int semitones) {
        if (!isMutable()) {
            throw new IllegalStateException("AddIntervalAboveModifier is immutable");
        }
        if (semitones <= 0) {
            return false;
        }
        this.semitones = semitones;
        return true;
    }
}
