package io.github.bmb0136.maestro.playback;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.Note;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public class PlaybackActionQueue {
    private final ArrayList<Action> queue = new ArrayList<>();
    private int offset;

    public Action get(int index) {
        return queue.get(offset + index);
    }

    @Nullable
    public Action getHead() {
        if (size() > 0) {
            return get(0);
        }
        return null;
    }

    @Nullable
    public Action getTail() {
        if (size() > 0) {
            return get(size() - 1);
        }
        return null;
    }

    public int size() {
        return queue.size() - offset;
    }

    public void add(@NotNull Action action) {
        if (!queue.isEmpty() && action.timeBeats < queue.getLast().timeBeats) {
            throw new IllegalArgumentException("Action %s inserted out of order".formatted(action));
        }
        queue.add(action);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        if (offset < 0 || offset > queue.size()) {
            throw new IndexOutOfBoundsException();
        }
        this.offset = offset;
    }

    public void seek(float beats) {
        // If past last action (or if empty), seek to end
        if (Optional.ofNullable(getTail()).map(a -> beats > a.timeBeats).orElse(true)) {
            offset = queue.size();
            return;
        }

        int L = 0;
        int R = queue.size();
        while (L < R) {
            var mid = (L + R) / 2;
            if (queue.get(mid).timeBeats < beats) {
                L = mid + 1;
            } else {
                R = mid - 1;
            }
        }
        setOffset(L);
    }

    public void addFromClip(@NotNull Clip clip) {
        PriorityQueue<Action> actions = new PriorityQueue<>(Comparator.comparing(Action::timeBeats));
        for (Note note : clip) {
            int midi = note.pitch().toMidi();
            var start = clip.getPosition() + note.position();
            actions.add(new Action(true, midi, (int) (note.volume() * 128), start));
            actions.add(new Action(true, midi, 0, start + note.duration()));
        }
        actions.forEach(this::add);
    }

    public record Action(boolean on, int note, int velocity, float timeBeats) {
    }
}
