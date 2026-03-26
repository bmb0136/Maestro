package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;

public class NoteEvent {
    public enum Type{OFF, ON}
    private final Type type;
    private final Note note;
    private final Pitch pitch;
    private final double timeExecution;

    public NoteEvent(Type type, Note note, Pitch pitch, double timeExecution){
        this.type = type;
        this.note = note;
        this.pitch = pitch;
        this.timeExecution = timeExecution;

    }
    //Returns Type of NoteEvent (ChordClip, PianoRoll, etc.)
    public Type getType(){return this.type;}
    //Returns Note of NoteEvent
    public Note getNote(){return this.note;}
    //Returns Pitch of NoteEvent
    public Pitch getPitch(){return this.pitch;}
    //Returns TimExecution (Time execution is based on the BPM and the Clips' Position
    public double getTimeExecution(){return this.timeExecution;}
}
