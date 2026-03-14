package io.github.bmb0136.maestro.core.modifier;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class ModifierList implements Iterable<Modifier> {
    private final ArrayList<Modifier> modifiers = new ArrayList<>();
    private boolean isMutable;

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

    public boolean removeModifier(UUID id) {
        for (int i = 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).getId().equals(id)) {
                modifiers.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeModifier(int index) {
        if (index < 0 || index >= modifiers.size()) {
            return false;
        }
        modifiers.remove(index);
        return true;
    }

    public void addModifier(@NotNull Modifier modifier) {
        modifiers.add(modifier);
    }

    public boolean moveNext(int index) {
        if (index < 0 || index + 1 >= modifiers.size()) {
            return false;
        }
        var temp = modifiers.get(index);
        modifiers.set(index, modifiers.get(index + 1));
        modifiers.set(index + 1, temp);
        return true;
    }

    public boolean movePrevious(int index) {
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

    public boolean isMutable() {
        return isMutable;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }

    public ModifierList copy(boolean newId) {
        ModifierList copy = new ModifierList();
        for (Modifier m : modifiers) {
            copy.modifiers.add(m.copy(newId));
        }
        return copy;
    }

    @Override
    public @NotNull Iterator<Modifier> iterator() {
        return modifiers.iterator();
    }
}
