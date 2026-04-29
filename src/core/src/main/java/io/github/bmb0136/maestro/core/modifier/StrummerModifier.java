package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.util.NoteUtils;
import io.github.bmb0136.maestro.core.util.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrummerModifier extends Modifier {
    private int divisions = 8;
    private PatternItem[] pattern = new PatternItem[divisions];

    public StrummerModifier() {
        Arrays.fill(pattern, PatternItem.OFF);
    }

    public int getDivisions() {
        return divisions;
    }

    public void setDivisions(int divisions) {
        if (!isMutable()) {
            throw new IllegalStateException("StrummerModifier is immutable");
        }
        if (divisions < 1) {
            throw new IllegalArgumentException("StrummerModifier: divisions must be >= 1");
        }
        this.divisions = divisions;
        PatternItem[] newPattern = new PatternItem[divisions];
        System.arraycopy(pattern, 0, newPattern, 0, Math.min(pattern.length, newPattern.length));
        pattern = newPattern;
    }

    public boolean setPatternItem(int i, boolean value) {
        if (!isMutable()) {
            throw new IllegalStateException("StrummerModifier is immutable");
        }
        if (i < 0 || i >= pattern.length) {
            return false;
        }

        if (i == 0) {
            switch (pattern[0]) {
                case OFF -> {
                    pattern[0] = value ? PatternItem.ON : PatternItem.OFF;
                    return value;
                }
                case ON -> {
                    pattern[0] = value ? PatternItem.ON : PatternItem.OFF;
                    return !value;
                }
                // Can't be CONTINUE because there's no note to continue
                default -> throw new IllegalStateException("Unexpected value: " + pattern[0]);
            }
        }

        var oldValue = pattern[i];
        pattern[i] = switch (pattern[i]) {
            case OFF, CONTINUE -> value ? PatternItem.ON : PatternItem.OFF;
            case ON -> value ? PatternItem.CONTINUE : PatternItem.OFF;
        };

        return pattern[i] != oldValue;
    }

    public List<PatternItem> getPattern() {
        return List.of(pattern);
    }

    @Override
    protected Modifier createCopy(boolean newId) {
        var copy = new StrummerModifier();
        copy.setMutable(true);
        copy.divisions = divisions;
        copy.pattern = new PatternItem[divisions];
        System.arraycopy(pattern, 0, copy.pattern, 0, pattern.length);
        copy.setMutable(false);
        return copy;
    }

    @Override
    public void applyTo(@NotNull List<Note> notes) {
        if (divisions == 1) {
            switch (pattern[0]) {
                case OFF -> {
                    notes.clear();
                    return;
                }
                case ON -> {
                    return;
                }
                default -> throw new IllegalStateException("Unexpected value: " + pattern[0]);
            }
        }

        var positions = renderPatternPositions();

        ArrayList<Note> output = new ArrayList<>();

        for (var g : NoteUtils.groupByPosition(notes)) {
            var grouped = g.second();
            float start = g.first();
            float duration = g.second().getFirst().duration();

            for (Tuple2<Float, Float> pos : positions) {
                for (Note note : grouped) {
                    output.add(note
                            .modifyPosition(ignored -> start + (duration * pos.first()))
                            .modifyDuration(ignored -> duration * pos.second()));
                }
            }
        }

        notes.clear();
        if (notes instanceof ArrayList<Note> arr) {
            arr.ensureCapacity(output.size());
        }
        notes.addAll(output);
    }

    private @NotNull ArrayList<Tuple2<Float, Float>> renderPatternPositions() {
        ArrayList<Tuple2<Float, Float>> positions = new ArrayList<>();
        final float delta = 1.0f / divisions;
        for (int i = 0; i < pattern.length; i++) {
            switch (pattern[i]) {
                case OFF -> { }
                case ON -> positions.add(new Tuple2<>(i * delta, delta));
                case CONTINUE -> {
                    if (i == 0) {
                        throw new AssertionError("First item of StrummerModifier pattern cannot be CONTINUE");
                    }
                    if (pattern[i - 1] == PatternItem.OFF) {
                        throw new AssertionError("CONTINUE item of StrummerModifier pattern must come after ON or CONTINUE");
                    }
                    var old = positions.getLast();
                    positions.set(positions.size() - 1, new Tuple2<>(old.first(), old.second() + delta));
                }
            }
        }
        return positions;
    }

    public enum PatternItem {
        OFF,
        ON,
        CONTINUE
    }
}
