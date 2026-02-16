package io.github.bmb0136.maestro.core.clip;

import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PianoRollClip extends Clip {
    private final ArrayList<Note> notes = new ArrayList<>();

    public PianoRollClip() {
        super();
    }

    protected PianoRollClip(UUID id) {
        super(id);
    }

    /**
     * Add a {@link Note} to this {@link PianoRollClip}
     *
     * @param note The {@link Note} to add
     * @return {@code true} if the note was added successfully
     */
    public boolean addNote(@NotNull Note note) {
        if (!isMutable()) {
            throw new IllegalStateException("PianoRollClip is immutable");
        }
        if (notes.isEmpty()) {
            notes.add(note);
            return true;
        }

        // Find first note whose position is equal to the given note
        var target = note.position();
        int l = 0;
        int r = notes.size() - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            var other = notes.get(mid);
            float diff = other.position() - target;
            if (Math.abs(diff) < 1e-6f) {
                if (other.pitch().equals(note.pitch())) {
                    return false;
                }
                l = mid + 1;
            } else if (diff < 0) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        // Check all equal position notes for duplicate pitches (binary search may not hit all of them)
        // Since there are only 128 possible MIDI pitches this is O(128) instead of O(n)
        int i = l - 1;
        while (i >= 0 && Math.abs(target - notes.get(i).position()) < 1e-6f) {
            if (notes.get(i).pitch().equals(note.pitch())) {
                return false;
            }
            i--;
        }

        notes.add(l, note);
        return true;
    }

    /**
     * Removes the first {@link Note} from this {@link PianoRollClip} with the specified {@link Pitch} that also contains the given position
     * <br>
     * This method functions as a "remove at cursor" method
     *
     * @param pitch The pitch of the {@link Note} to remove
     * @param position The position the removed {@link Note} must contain
     * @return {@code true} if any notes were removed
     */
    public boolean removeNote(@NotNull Pitch pitch, float position) {
        // IntelliJ is being silly
        //noinspection ExtractMethodRecommender
        if (!isMutable()) {
            throw new IllegalStateException("PianoRollClip is immutable");
        }

        // Find first note whose right side (position + duration) is before position
        int l = 0;
        int r = notes.size() - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            Note other = notes.get(mid);
            float cmp = other.position() + other.duration() - position;
            if (cmp < 0) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        // Scan right to find first note that contains
        int i = r + 1;
        while (i < notes.size()) {
            Note n = notes.get(i);
            // If note is after position then we didn't find anything
            if (n.position() >= position) {
                break;
            }
            // Check that we are inside the note
            if (position < n.position() + n.duration() && n.pitch().equals(pitch)) {
                notes.remove(i);
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    protected Clip createCopy(boolean newId) {
        PianoRollClip copy = new PianoRollClip(newId ? UUID.randomUUID() : getId());
        copy.setMutable(true);
        copy.notes.addAll(notes);
        copy.setMutable(false);
        return copy;
    }

        // Scan right to find first note that contains
        int i = r + 1;
        while (i < notes.size()) {
            Note n = notes.get(i);
            // If note is after position then we didn't find anything
            if (n.position() >= position) {
                break;
            }
            // Check that we are inside the note
            if (position < n.position() + n.duration() && n.pitch().equals(pitch)) {
                notes.remove(i);
                return true;
            }
            i++;
        }
        return false;
    }

    public static PianoRollClip create(float position, float duration) {
        return create(position, duration, Collections.emptyList());
    }

    public static PianoRollClip create(float position, float duration, Iterable<Note> notes) {
        var clip = new PianoRollClip();
        clip.setMutable(true);
        clip.setPosition(position);
        clip.setPosition(duration);
        notes.forEach(clip::addNote);
        clip.setMutable(false);
        return clip;
    }
}
