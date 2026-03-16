package io.github.bmb0136.maestro.core.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EventResult {
    public static final EventResult OK = new EventResult("OK", true);
    public static final EventResult NOOP = new EventResult("NOOP", true);
    public static final EventResult UNKNOWN_TRACK = new EventResult("UNKNOWN_TRACK");
    public static final EventResult UNKNOWN_CLIP = new EventResult("UNKNOWN_CLIP");
    public static final EventResult CLIP_ALREADY_ON_TIMELINE = new EventResult("CLIP_ALREADY_ON_TIMELINE");
    public static final EventResult TRACK_ALREADY_ON_TIMELINE = new EventResult("TRACK_ALREADY_ON_TIMELINE");
    public static final EventResult INVALID_CLIP_DURATION = new EventResult("INVALID_CLIP_DURATION");
    public static final EventResult INVALID_CLIP_POSITION = new EventResult("INVALID_CLIP_POSITION");
    public static final EventResult CLIP_OVERLAP = new EventResult("CLIP_OVERLAP");
    public static final EventResult WRONG_CLIP_TYPE = new EventResult("WRONG_CLIP_TYPE");
    public static final EventResult NOTE_OUTSIDE_CLIP = new EventResult("NOTE_OUTSIDE_CLIP");

    @NotNull
    private final String name;
    private final boolean ok;
    private final List<EventTarget> targets;

    private EventResult(@NotNull String name, boolean ok, List<EventTarget> targets) {
        this.name = name;
        this.ok = ok;
        this.targets = targets;
    }

    private EventResult(@NotNull String name, boolean ok) {
        this(name, ok, Collections.emptyList());
    }

    private EventResult(@NotNull String name, List<EventTarget> targets) {
        this(name, false, targets);
    }

    private EventResult(@NotNull String name) {
        this(name, Collections.emptyList());
    }

    public boolean isOk() {
        return ok;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public List<EventTarget> getTargets() {
        return targets;
    }

    public EventResult withTargets(List<EventTarget> targets) {
        return new EventResult(name, ok, Collections.unmodifiableList(targets));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EventResult other && name.equals(other.getName());
    }

    // TODO: come up with something better
    @Override
    public String toString() {
        // Convert `UPPER_SNAKE_CASE` to `Title Case`
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            var c = chars[i];
            if (c == '_') {
                chars[i] = ' ';
            } else {
                chars[i] = i < 1 || chars[i - 1] == ' ' ? Character.toUpperCase(c) : Character.toLowerCase(c);
            }
        }
        return new String(chars);
    }
}
