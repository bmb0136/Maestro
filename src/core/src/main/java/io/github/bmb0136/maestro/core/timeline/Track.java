package io.github.bmb0136.maestro.core.timeline;

import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class Track implements Iterable<Clip> {
    private final HashMap<UUID, Clip> clips = new HashMap<>();
    private final UUID id;
    private boolean mutable;

    public Track() {
        this(UUID.randomUUID());
    }

    protected Track(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public @NotNull Iterator<Clip> iterator() {
        return clips.values().iterator();
    }

    public boolean hasClip(@NotNull UUID id) {
        return clips.containsKey(id);
    }

    public Optional<Clip> getClip(@NotNull UUID id) {
        return Optional.ofNullable(clips.getOrDefault(id, null));
    }

    public void addClip(@NotNull Clip clip) {
        if (!mutable) {
            throw new IllegalStateException("Track is immutable");
        }
        clips.put(clip.getId(), clip);
    }

    public boolean removeClip(@NotNull UUID clipId) {
        if (!mutable) {
            throw new IllegalStateException("Track is immutable");
        }
        return clips.remove(clipId) != null;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public Track copy(boolean newId) {
        var copy = new Track(newId ? UUID.randomUUID() : id);
        copy.setMutable(true);
        forEach(c -> copy.addClip(c.copy(newId)));
        copy.setMutable(false);
        return copy;
    }
}
