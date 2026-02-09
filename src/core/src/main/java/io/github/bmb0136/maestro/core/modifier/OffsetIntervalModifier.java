package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.Note;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OffsetIntervalModifier extends Modifier {
    private int semitones;

    @Override
    public void applyTo(@NotNull List<Note> input) {
        if (semitones == 0) {
            return;
        }
        input.replaceAll(note -> note.modifyPitch(p -> p.addSemitones(semitones)));
    }

    @Override
    protected Modifier createCopy() {
        var copy = new OffsetIntervalModifier();
        copy.setSemitones(semitones);
        return copy;
    }

    public int getSemitones() {
        return semitones;
    }

    public void setSemitones(int semitones) {
        this.semitones = semitones;
    }
}
