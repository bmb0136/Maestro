package io.github.bmb0136.maestro.core.modifier;

import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;

public class ModifierList implements Iterable<Modifier> {
    private final ArrayList<Modifier> modifiers = new ArrayList<>();
    @Nullable
    private final BooleanSupplier mutabilityCheck;

    public ModifierList() {
        this(null);
    }

    public ModifierList(@Nullable BooleanSupplier mutabilityCheck) {
        this.mutabilityCheck = mutabilityCheck;
    }

    public Optional<Modifier> getModifier(UUID id) {
        for (Modifier m : modifiers) {
            if (m.getId().equals(id)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public Optional<Modifier> getModifier(int index) {
        if (index < 0 || index >= modifiers.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(modifiers.get(index));
    }

    public OptionalInt indexOf(UUID id) {
        for (int i = 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).getId().equals(id)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    public boolean removeModifier(UUID id) {
        if (mutabilityCheck != null && !mutabilityCheck.getAsBoolean()) {
            throw new IllegalStateException("ModifierList is immutable");
        }
        for (int i = 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).getId().equals(id)) {
                modifiers.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeModifier(int index) {
        if (mutabilityCheck != null && !mutabilityCheck.getAsBoolean()) {
            throw new IllegalStateException("ModifierList is immutable");
        }
        if (index < 0 || index >= modifiers.size()) {
            return false;
        }
        modifiers.remove(index);
        return true;
    }

    public void addModifier(@NotNull Modifier modifier) {
        if (mutabilityCheck != null && !mutabilityCheck.getAsBoolean()) {
            throw new IllegalStateException("ModifierList is immutable");
        }
        modifiers.add(modifier);
    }

    public boolean moveNext(int index) {
        if (mutabilityCheck != null && !mutabilityCheck.getAsBoolean()) {
            throw new IllegalStateException("ModifierList is immutable");
        }
        if (index < 0 || index + 1 >= modifiers.size()) {
            return false;
        }
        var temp = modifiers.get(index);
        modifiers.set(index, modifiers.get(index + 1));
        modifiers.set(index + 1, temp);
        return true;
    }

    public boolean movePrevious(int index) {
        if (mutabilityCheck != null && !mutabilityCheck.getAsBoolean()) {
            throw new IllegalStateException("ModifierList is immutable");
        }
        if (index < 1 || index >= modifiers.size()) {
            return false;
        }
        var temp = modifiers.get(index);
        modifiers.set(index, modifiers.get(index - 1));
        modifiers.set(index - 1, temp);
        return true;
    }

    public int size() {
        return modifiers.size();
    }

    public void applyTo(@NotNull List<Note> notes) {
        modifiers.forEach(m -> m.applyTo(notes));
    }

    @Override
    public @NotNull Iterator<Modifier> iterator() {
        return modifiers.iterator();
    }
}
