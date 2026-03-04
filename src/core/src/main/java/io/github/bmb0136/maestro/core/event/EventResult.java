package io.github.bmb0136.maestro.core.event;

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

    private final String name;
    private final boolean ok;

    private EventResult(String name, boolean ok) {
        this.name = name;
        this.ok = ok;
    }

    private EventResult(String name) {
        this(name, false);
    }

    public boolean isOk() {
        return ok;
    }

    public String getName() {
        return name;
    }
}
