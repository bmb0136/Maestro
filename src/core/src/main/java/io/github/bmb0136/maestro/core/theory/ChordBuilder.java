package io.github.bmb0136.maestro.core.theory;

import io.github.bmb0136.maestro.core.util.Tuple2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChordBuilder {
    private final TreeSet<Pitch> pitches = new TreeSet<>(Comparator.comparingInt(Pitch::toMidi));
    private final View view = new View();
    @NotNull
    private PitchName rootNote = PitchName.C;
    private int inversionNumber = 0;
    @Nullable
    private PitchName bassNote = null;
    @NotNull
    private ChordQuality quality = ChordQuality.MAJOR;
    private int baseOctave = 4;
    private final HashMap<Integer, Accidental> alterations = new HashMap<>();

    public View getView() {
        return view;
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

        // Reset notes to original order to find inversion
        bassNote = null;
        inversionNumber = 0;
        recalculatePitches();

        int i = 0;
        for (Pitch pitch : pitches) {
            if (pitch.name().isEnharmonicallyEquivalentTo(slashNote)) {
                inversionNumber = i;
                bassNote = null;
                recalculatePitches();
                return this;
            }
            i++;
        }

        bassNote = slashNote;
        inversionNumber = 0;
        recalculatePitches();
        return this;
    }

    public ChordBuilder setQuality(@NotNull ChordQuality quality) {
        this.quality = quality;
        // Update inversion if new quality contains current slash note
        return setSlashNote(view.getSlashNote());
    }

    public ChordBuilder setBaseOctave(int baseOctave) {
        this.baseOctave = baseOctave;
        recalculatePitches();
        return this;
    }

    public ChordBuilder addAlteration(@NotNull Accidental accidental, int scaleDegree) {
        if (scaleDegree < 1 || scaleDegree > 15) {
            throw new IllegalArgumentException("Invalid chord alteration scale degree: " + scaleDegree);
        }
        alterations.put(scaleDegree, accidental);
        return this;
    }

    public ChordBuilder removeAlteration(@NotNull Accidental accidental, int scaleDegree) {
        Accidental removed = alterations.remove(scaleDegree);
        if (accidental != removed) {
            return addAlteration(removed, scaleDegree);
        }
        return this;
    }

    public ChordBuilder removeAlteration(int scaleDegree) {
        alterations.remove(scaleDegree);
        return this;
    }

    private void recalculatePitches() {
        pitches.clear();
        var rootPitch = new Pitch(rootNote, baseOctave);
        pitches.add(rootPitch);

        // Add intervals based on chord quality
        for (int interval : quality.getIntervalsFromRoot()) {
            pitches.add(rootPitch.addSemitones(interval, quality.getKeySignature(rootNote).isSharpKey()));
        }

        var scale = ScaleFactory.create(ScaleType.MAJOR, rootNote);
        for (var alt : alterations.entrySet()) {
            var pitch = rootPitch.nextAbove(scale.getDegree(alt.getKey() - 1));
            switch (alt.getValue()) {
                case SHARP -> pitch = pitch.addSemitones(1, true);
                case FLAT -> pitch = pitch.addSemitones(-1, false);
                default -> {}
            }
            var name = pitch.name();
            pitches.removeIf(p -> p.name().isEnharmonicallyEquivalentTo(name));
        }

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

    public ChordBuilder copy() {
        var b = new ChordBuilder();
        b.rootNote = rootNote;
        b.quality = quality;
        b.inversionNumber = inversionNumber;
        b.bassNote = bassNote;
        b.baseOctave = baseOctave;
        b.alterations.putAll(alterations);
        b.recalculatePitches();
        return b;
    }

    public Chord build() {
        recalculatePitches();
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
        PitchName slashNote = view.getSlashNote();
        if (slashNote != null) {
            sb.append('/');
            sb.append(slashNote);
        }

        // Append extensions/alterations
        boolean addParens = alterations.size() > 1;
        if (addParens) {
            sb.append('(');
        }
        for (var it = alterations.entrySet().iterator(); it.hasNext(); ) {
            var alt = it.next();
            switch (alt.getValue()) {
                case SHARP -> sb.append('#');
                case FLAT -> sb.append('b');
            }
            sb.append(alt.getKey());
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        if (addParens) {
            sb.append(')');
        }

        return sb.toString();
    }

    // A read-only wrapper around this builder
    public class View implements Iterable<Pitch> {
        public PitchName getRootNote() {
            return rootNote;
        }

        public ChordQuality getQuality() {
            return quality;
        }

        public int getBaseOctave() {
            return baseOctave;
        }

        @Nullable
        public PitchName getSlashNote() {
            if (bassNote != null) {
                return bassNote;
            } else if (inversionNumber != 0 && !pitches.isEmpty()) {
                return pitches.getFirst().name();
            }
            return null;
        }

        public String getChordName() {
            return ChordBuilder.this.getChordName();
        }

        public List<Tuple2<Integer, Accidental>> getAlterations() {
            var list = new ArrayList<Tuple2<Integer, Accidental>>(alterations.size());
            for (var alt : alterations.entrySet()) {
                list.add(new Tuple2<>(alt.getKey(), alt.getValue()));
            }
            list.sort(Comparator.comparingInt(Tuple2::first));
            return Collections.unmodifiableList(list);
        }

        @Override
        public @NotNull Iterator<Pitch> iterator() {
            recalculatePitches();
            return new ArrayList<>(pitches).iterator();
        }
    }
}
