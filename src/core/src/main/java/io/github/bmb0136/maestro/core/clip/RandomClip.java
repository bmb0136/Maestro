package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.theory.ScaleType;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.UUID;

public class RandomClip extends Clip {
    private ScaleType scale = ScaleType.MAJOR;
    private PitchName rootPitch = PitchName.C;
    private int rootOctave = 4;
    private int minDegree = 0, maxDegree = 7;

    public RandomClip() {
        this(UUID.randomUUID());
    }

    protected RandomClip(UUID id) {
        super(id);
    }

    public ScaleType getScale() {
        return scale;
    }

    public void setScale(ScaleType scale) {
        if (!isMutable()) {
            throw new IllegalStateException("RandomClip is immutable");
        }
        this.scale = scale;
    }

    public PitchName getRootPitch() {
        return rootPitch;
    }

    public void setRootPitch(PitchName rootPitch) {
        if (!isMutable()) {
            throw new IllegalStateException("RandomClip is immutable");
        }
        this.rootPitch = rootPitch;
    }

    public int getRootOctave() {
        return rootOctave;
    }

    public void setRootOctave(int rootOctave) {
        if (!isMutable()) {
            throw new IllegalStateException("RandomClip is immutable");
        }
        this.rootOctave = rootOctave;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public void setMinDegree(int minDegree) {
        if (!isMutable()) {
            throw new IllegalStateException("RandomClip is immutable");
        }
        this.minDegree = minDegree;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        if (!isMutable()) {
            throw new IllegalStateException("RandomClip is immutable");
        }
        this.maxDegree = maxDegree;
    }


    @Override
    protected Clip createCopy(boolean newId) {
        var clip = new RandomClip(newId ? UUID.randomUUID() : getId());
        clip.setMutable(true);
        clip.setScale(scale);
        clip.setRootPitch(rootPitch);
        clip.setRootOctave(rootOctave);
        clip.setMinDegree(minDegree);
        clip.setMaxDegree(maxDegree);
        clip.setMutable(false);
        return clip;
    }

    @Override
    public @NotNull Iterator<Note> iterator() {
        throw new RuntimeException("TODO");
    }
}
