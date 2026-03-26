package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.theory.ScaleFactory;
import io.github.bmb0136.maestro.core.theory.ScaleType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class ScaleClip extends Clip {
    private ScaleType scale = ScaleType.MAJOR;
    private PitchName rootPitch = PitchName.C;
    private int rootOctave = 4;
    private int minDegree = 0, maxDegree = 7;
    private float noteDuration = 0.25f;
    private boolean repeat = true;
    private Mode mode = Mode.ASCENDING;

    public ScaleClip() {
        this(UUID.randomUUID());
    }

    protected ScaleClip(UUID id) {
        super(id);
    }

    public ScaleType getScale() {
        return scale;
    }

    public void setScale(ScaleType scale) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.scale = scale;
    }

    public PitchName getRootPitch() {
        return rootPitch;
    }

    public void setRootPitch(PitchName rootPitch) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.rootPitch = rootPitch;
    }

    public int getRootOctave() {
        return rootOctave;
    }

    public void setRootOctave(int rootOctave) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.rootOctave = rootOctave;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public void setMinDegree(int minDegree) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.minDegree = minDegree;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.maxDegree = maxDegree;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.mode = mode;
    }

    public float getNoteDuration() {
        return noteDuration;
    }

    public void setNoteDuration(float noteDuration) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.noteDuration = noteDuration;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        if (!isMutable()) {
            throw new IllegalStateException("ScaleClip is immutable");
        }
        this.repeat = repeat;
    }

    @Override
    protected Clip createCopy(boolean newId) {
        var clip = new ScaleClip(newId ? UUID.randomUUID() : getId());
        clip.setMutable(true);
        clip.setScale(scale);
        clip.setRootPitch(rootPitch);
        clip.setRootOctave(rootOctave);
        clip.setMinDegree(minDegree);
        clip.setMaxDegree(maxDegree);
        clip.setMode(mode);
        clip.setRepeat(repeat);
        clip.setNoteDuration(noteDuration);
        clip.setMutable(false);
        return clip;
    }

    @Override
    public @NotNull Iterator<Note> iterator() {
        ArrayList<Note> notes = new ArrayList<>();
        var scale = ScaleFactory.create(this.scale, rootPitch);
        switch (mode) {
            case HOLD -> {
                for (int i = minDegree; i <= maxDegree; i++) {
                    notes.add(new Note(scale.getPitch(i, rootOctave), 0, getDuration()));
                }
            }
            case ASCENDING -> {
                float position = 0;
                int i = minDegree;
                while (position < getDuration()) {
                    if (i > maxDegree) {
                        if (isRepeat()) {
                            i = minDegree;
                        } else {
                            break;
                        }
                    }

                    notes.add(new Note(scale.getPitch(i, rootOctave), position, noteDuration));

                    i++;
                    position += noteDuration;
                }
            }
            case DESCENDING -> {
                float position = 0;
                int i = maxDegree;
                while (position < getDuration()) {
                    if (i < minDegree) {
                        if (isRepeat()) {
                            i = maxDegree;
                        } else {
                            break;
                        }
                    }

                    notes.add(new Note(scale.getPitch(i, rootOctave), position, noteDuration));

                    i--;
                    position += noteDuration;
                }
            }
        }
        return notes.iterator();
    }

    public enum Mode {
        HOLD,
        ASCENDING,
        DESCENDING
    }
}
