package io.github.bmb0136.maestro.core.timeline;

import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class Timeline implements Iterable<Track> {
    private final ArrayList<Track> tracks = new ArrayList<>();
    private boolean mutable;

    @Override
    public @NotNull Iterator<Track> iterator() {
        return tracks.iterator();
    }

    public boolean hasTrack(@NotNull UUID id) {
        for (Track track : tracks) {
            if (track.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Track> getTrack(@NotNull UUID id) {
        for (Track track : tracks) {
            if (track.getId().equals(id)) {
                return Optional.of(track);
            }
        }
        return Optional.empty();
    }

    public Track getTrack(int index) {
        return tracks.get(index);
    }

    public Optional<UUID> getTrackForClip(UUID id) {
        for (Track track : tracks) {
            if (track.getClip(id).isPresent()) {
                return Optional.of(track.getId());
            }
        }
        return Optional.empty();
    }

    public Optional<Clip> getClip(UUID id) {
        for (Track track : tracks) {
            var opt = track.getClip(id);
            if (opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    public void addTrack(@NotNull Track track) {
        if (!mutable) {
            throw new IllegalStateException("Timeline is immutable");
        }
        tracks.add(track);
    }

    public boolean removeTrack(UUID id) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId().equals(id)) {
                tracks.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean moveNext(int index) {
        if (!isMutable()) {
            throw new IllegalStateException("Timeline is immutable");
        }
        if (index < 0 || index + 1 >= tracks.size()) {
            return false;
        }
        var temp = tracks.get(index);
        tracks.set(index, tracks.get(index + 1));
        tracks.set(index + 1, temp);
        return true;
    }

    public boolean movePrevious(int index) {
        if (!isMutable()) {
            throw new IllegalStateException("Timeline is immutable");
        }
        if (index < 1 || index >= tracks.size()) {
            return false;
        }
        var temp = tracks.get(index);
        tracks.set(index, tracks.get(index - 1));
        tracks.set(index - 1, temp);
        return true;
    }

    public int size() {
        return tracks.size();
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public Timeline copy(boolean newId) {
        var copy = new Timeline();
        copy.setMutable(true);
        tracks.forEach(t -> copy.addTrack(t.copy(newId)));
        copy.setMutable(false);
        return copy;
    }

    public int indexOf(@NotNull UUID trackId) {
        for (int i = 0; i < tracks.size(); i++) {
            if (trackId.equals(tracks.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    public float getDuration() {
        float duration = 0;
        for (Track track : tracks) {
            duration = Math.max(duration, track.getDuration());
        }
        return duration;
    }
}
