package io.github.bmb0136.maestro.core.theory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChordBuilder {
    @NotNull
    private PitchName rootNote = PitchName.C;
    private int inversionNumber = 0;
    @Nullable
    private PitchName bassNote = null;
    @NotNull
    private ChordQuality quality = ChordQuality.MAJOR;
    private final TreeSet<Pitch> pitches = new TreeSet<>(Comparator.comparingInt(Pitch::toMidi));
    private int baseOctave = 4;

    public @NotNull PitchName getRootNote() {
        return rootNote;
    }

    public ChordBuilder setRootNote(@NotNull PitchName pitch) {
        rootNote = pitch;
        recalculatePitches();
        return this;
    }

    public ChordBuilder setSlashNote(@Nullable PitchName slashNote) {
        // Clear inversion or bass note if null or if same as root note
        if (slashNote == null || slashNote.isEnharmonicallyEquivalentTo(rootNote)) {
            inversionNumber = 0;
            bassNote = null;
            recalculatePitches();
            return this;
        }

        int i = 0;
        int start = bassNote != null ? 1 : 0;
        for (Pitch pitch : pitches) {
            if (i++ < start) {
                continue;
            }
            if (pitch.name().isEnharmonicallyEquivalentTo(slashNote)) {
                inversionNumber = i - start + 1;
                bassNote = null;
                recalculatePitches();
                return this;
            }
        }

        bassNote = slashNote;
        recalculatePitches();
        return this;
    }

    public int getInversionNumber() {
        return inversionNumber;
    }

    public @Nullable PitchName getBassNote() {
        return bassNote;
    }

    public @NotNull ChordQuality getQuality() {
        return quality;
    }

    public ChordBuilder setQuality(@NotNull ChordQuality quality) {
        this.quality = quality;
        recalculatePitches();
        return this;
    }

    public int getBaseOctave() {
        return baseOctave;
    }

    public void setBaseOctave(int baseOctave) {
        this.baseOctave = baseOctave;
    }

    private void recalculatePitches() {
        pitches.clear();
        var rootPitch = new Pitch(rootNote, baseOctave);
        pitches.add(rootPitch);

        // Add intervals based on chord quality
        for (int interval : quality.getIntervalsFromRoot()) {
            pitches.add(rootPitch.addSemitones(interval, quality.getKeySignature(rootNote).isSharpKey()));
        }

        // TODO: add extensions

        // Invert
        for (int i = 0; i < inversionNumber; i++) {
            var temp = pitches.removeFirst();
            pitches.add(new Pitch(temp.name(), temp.octave() + 1));
        }
        // Shift everything down to base octave if necessary
        while (pitches.first().octave() > baseOctave) {
            // Copy to temp list to avoid ConcurrentModificationException
            List.copyOf(pitches).forEach(p -> {
                pitches.remove(p);
                pitches.add(new Pitch(p.name(), p.octave() - 1));
            });
        }

        // Add base note if applicable
        if (bassNote != null) {
            pitches.add(new Pitch(bassNote, baseOctave - 1));
        }
    }

    public Chord build() {
        Pitch[] pitches = new Pitch[this.pitches.size()];
        int i = 0;
        for (Pitch pitch : this.pitches) {
            pitches[i++] = pitch;
        }
        return new Chord(pitches);
    }

    public String getChordName() {
        recalculatePitches();

        StringBuilder sb = new StringBuilder();
        sb.append(rootNote);

        // Append quality
        sb.append(switch (quality) {
            case MAJOR -> "maj";
            case MINOR -> "min";
            case AUGMENTED -> "+";
            case DIMINISHED_TRIAD -> "o";
            case DIMINISHED_SEVENTH -> "o7";
            case HALF_DIMINISHED -> "ø";
            case SUS2 -> "sus2";
            case SUS4 -> "sus4";
        });

        // Get slash note (either bass note or inversion) and append
        PitchName slashNote = null;
        if (bassNote != null) {
            slashNote = bassNote;
        } else if (inversionNumber != 0) {
            int i = inversionNumber;
            for (var p : pitches) {
                slashNote = p.name();
                if (i-- < 0) {
                    break;
                }
            }
        }
        if (slashNote != null) {
            sb.append('/');
            sb.append(slashNote);
        }

        return sb.toString();
    }
}
