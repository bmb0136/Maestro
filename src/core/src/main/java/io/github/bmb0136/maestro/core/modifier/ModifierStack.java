package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.Note;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModifierStack implements Iterable<Modifier> {
    private final ArrayList<Modifier> modifiers = new ArrayList<>();

    public int size() {
        return modifiers.size();
    }

    public boolean isEmpty() {
        return modifiers.isEmpty();
    }

    public Modifier get(int i) {
        return modifiers.get(i);
    }

    public void remove(int i) {
        modifiers.remove(i);
    }

    public void movePrevious(int i) {
        if (i == 0) {
            return;
        }

        var temp = get(i);
        modifiers.set(i, get(i - 1));
        modifiers.set(i - 1, temp);
    }

    public void moveNext(int i) {
        if (i == size() - 1) {
            return;
        }

        var temp = get(i);
        modifiers.set(i, get(i + 1));
        modifiers.set(i + 1, temp);
    }

    public @NotNull List<Note> apply(@NotNull List<Note> notes) {
        for (Modifier modifier : modifiers) {
            modifier.applyTo(notes);
        }
        return notes;
    }

    @Override
    public @NotNull Iterator<Modifier> iterator() {
        return modifiers.iterator();
    }
}
