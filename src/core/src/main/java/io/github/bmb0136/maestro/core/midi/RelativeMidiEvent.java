package io.github.bmb0136.maestro.core.midi;

import io.github.bmb0136.maestro.core.Note;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.List;

/**
 * Alternative to {@link javax.sound.midi.MidiEvent} with time units of beats (instead of ticks).
 *
 * @param message The {@link MidiMessage} for this event
 * @param position The position of this event in beats
 */
public record RelativeMidiEvent(MidiMessage message, float position) {
    /**
     * Helper to convert {@link Note}s into {@link RelativeMidiEvent}s
     *
     * @param list The {@link List} to add the MIDI events to; must be mutable
     * @param note The {@link Note} to add to the given list
     */
    public static void addNoteToList(@NotNull List<RelativeMidiEvent> list, @NotNull Note note) {
        var midiPitch = note.pitch().toMidi();
        var velocity = (int)(note.volume() * 127);
        try {
            var on = new ShortMessage(ShortMessage.NOTE_ON, midiPitch, velocity);
            var off = new ShortMessage(ShortMessage.NOTE_OFF, midiPitch, 0);
            list.add(new RelativeMidiEvent(on, note.position()));
            list.add(new RelativeMidiEvent(off, note.position() + note.duration()));
        } catch (InvalidMidiDataException e) {
            throw new AssertionError("Note instances should always be valid MIDI", e);
        }
    }
}
