package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.util.NoteUtils;
import io.github.bmb0136.maestro.core.util.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ArpeggiatorModifier extends Modifier {
    private float noteDuration = 0.25f;
    private InputMode inputMode = InputMode.HELD;
    private OutputMode outputMode = OutputMode.ASCENDING;
    private int seed = 0;
    private boolean repeat = true;

    public float getNoteDuration() {
        return noteDuration;
    }

    public void setNoteDuration(float noteDuration) {
        if (!isMutable()) {
            throw new IllegalStateException("ArpeggiatorModifier is immutable");
        }
        this.noteDuration = noteDuration;
    }

    public InputMode getInputMode() {
        return inputMode;
    }

    public void setInputMode(InputMode inputMode) {
        if (!isMutable()) {
            throw new IllegalStateException("ArpeggiatorModifier is immutable");
        }
        this.inputMode = inputMode;
    }

    public OutputMode getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(OutputMode outputMode) {
        if (!isMutable()) {
            throw new IllegalStateException("ArpeggiatorModifier is immutable");
        }
        this.outputMode = outputMode;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        if (!isMutable()) {
            throw new IllegalStateException("ArpeggiatorModifier is immutable");
        }
        this.seed = seed;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        if (!isMutable()) {
            throw new IllegalStateException("ArpeggiatorModifier is immutable");
        }
        this.repeat = repeat;
    }

    @Override
    protected Modifier createCopy(boolean newId) {
        var copy = new ArpeggiatorModifier();
        copy.setMutable(true);
        copy.setInputMode(inputMode);
        copy.setOutputMode(outputMode);
        copy.setSeed(seed);
        copy.setRepeat(repeat);
        copy.setNoteDuration(noteDuration);
        copy.setMutable(false);
        return copy;
    }

    @Override
    public void applyTo(@NotNull List<Note> notes) {
        if (notes.isEmpty()) {
            return;
        }

        List<Tuple2<Float, List<Note>>> groups;
        switch (inputMode) {
            case HELD -> groups = NoteUtils.groupByPosition(notes);
            case ALL -> groups = List.of(new Tuple2<>(0f, notes));
            default -> throw new IllegalStateException("Unexpected value: " + inputMode);
        }


        for (var group : groups) {
            switch (outputMode) {
                case ASCENDING -> group.second().sort(Comparator.comparingInt(n -> n.pitch().toMidi()));
                case DESCENDING -> group.second().sort(Comparator.comparingInt(n -> -n.pitch().toMidi()));
                case RANDOM -> Collections.shuffle(group.second(), new Random(seed));
                default -> throw new IllegalStateException("Unexpected value: " + outputMode);
            }
        }

        ArrayList<Note> output = new ArrayList<>(notes.size());
        for (var tuple : groups) {
            var grouped = tuple.second();
            var start = tuple.first();
            var end = start + grouped.getFirst().duration();
            int i = 0;
            float position = start;
            while (position < end) {
                final var positionCopy = position;
                output.add(grouped.get(i)
                        .modifyPosition(ignored -> positionCopy)
                        .modifyDuration(ignored -> Math.min(noteDuration, end - positionCopy)));

                position += noteDuration;
                i++;
                if (i >= grouped.size()) {
                    if (repeat) {
                        i = 0;
                    } else {
                        break;
                    }
                }
            }
        }

        notes.clear();
        if (notes instanceof ArrayList<Note> arr) {
            arr.ensureCapacity(output.size());
        }
        notes.addAll(output);
    }

    public enum InputMode {
        HELD,
        ALL
    }

    public enum OutputMode {
        ASCENDING,
        DESCENDING,
        RANDOM
    }
}
