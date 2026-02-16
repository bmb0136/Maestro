package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;

public class PianoRollClipTest {
    @RepeatedTest(100)
    public void addNote_maintainsSortedOrder() {
        var clip = createRandomClip(10000);

        Note prev = null;
        for (Note note : clip) {
            if (prev == null) {
                prev = note;
                continue;
            }
            final Note tempPrev = prev;
            Assertions.assertTrue(note.position() - prev.position() > -1e-6f, () -> "Wrong order: %s@%f %s@%f".formatted(tempPrev.pitch(), tempPrev.position(), note.pitch(), note.position()));
        }
    }

    @RepeatedTest(100)
    public void removeNote_maintainsSortedOrder() {
        final int NUM_REMOVES = 1000;
        var clip = createRandomClip(NUM_REMOVES * 10);

        var notes = new ArrayList<>();
        clip.forEach(notes::add);

        for (int i = 0; i < NUM_REMOVES; i++) {
            notes.remove((int) (Math.random() * notes.size()));
        }


        Note prev = null;
        for (Note note : clip) {
            if (prev == null) {
                prev = note;
                continue;
            }
            final Note tempPrev = prev;
            Assertions.assertTrue(note.position() - prev.position() > -1e-6f, () -> "Wrong order: %s@%f %s@%f".formatted(tempPrev.pitch(), tempPrev.position(), note.pitch(), note.position()));
        }
    }

    private static PianoRollClip createRandomClip(int numNotes) {
        var clip = PianoRollClip.create(0f, 4f);

        clip.setMutable(true);
        for (int i = 0; i < numNotes; i++) {
            int pitch = (int) (Math.random() * 128);
            float position = (float) Math.random();
            float duration = (float) Math.random() + 0.1f;
            clip.addNote(new Note(Pitch.fromMidi(pitch, false), position, duration));
        }
        clip.setMutable(false);

        return clip;
    }
}
