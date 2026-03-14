package io.github.bmb0136.maestro.core.modifier;

import java.util.UUID;

public class OffsetByIntervalModifier extends Modifier {

    private int semitones = 1;

    public OffsetByIntervalModifier() {
        super();
    }

    protected OffsetByIntervalModifier(UUID uuid) {
        super(uuid);
    }

    @Override
    protected Modifier createCopy(boolean newId) {
        var copy = new OffsetByIntervalModifier(newId ? UUID.randomUUID() : getId());
        copy.semitones = semitones;
        return copy;
    }

    public int getSemitones() {
        return semitones;
    }

    public void setSemitones(int semitones) {
        if (!isMutable()) {
            throw new IllegalStateException("OffsetByIntervalModifier is immutable");
        }
        this.semitones = semitones;
    }
}
