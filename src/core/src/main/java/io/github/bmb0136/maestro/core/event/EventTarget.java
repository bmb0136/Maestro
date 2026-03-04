package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EventTarget {
    @NotNull
    private final Timeline timeline;
    private final Kind kind;
    @Nullable
    private final UUID trackId;
    @Nullable
    private final UUID clipId;

    private EventTarget(@NotNull Kind kind, @NotNull Timeline timeline, @Nullable UUID trackId, @Nullable UUID clipId) {
        this.timeline = timeline;
        this.kind = kind;
        this.trackId = trackId;
        this.clipId = clipId;
    }

    public boolean isTimeline() {
        return kind == Kind.TIMELINE;
    }

    public boolean isTrack() {
        return kind == Kind.TRACK;
    }

    public boolean isClip() {
        return kind == Kind.CLIP;
    }

    @NotNull
    public Timeline getTimeline() {
        return timeline;
    }

    public Optional<UUID> getTrackId() {
        return Optional.ofNullable(trackId);
    }

    public Optional<UUID> getClipId() {
        return Optional.ofNullable(clipId);
    }

    public static EventTarget timeline(@NotNull Timeline timeline) {
        return new EventTarget(Kind.TIMELINE, timeline, null, null);
    }

    public static EventTarget track(@NotNull Timeline timeline, @NotNull UUID trackId) {
        return new EventTarget(Kind.TRACK, timeline, trackId, null);
    }

    public static EventTarget clip(@NotNull Timeline timeline, @NotNull UUID trackId, @NotNull UUID clipId) {
        return new EventTarget(Kind.CLIP, timeline, trackId, clipId);
    }

    public static EventTarget fromEventContext(@NotNull EventContext<?> context) {
        return switch (context.target()) {
            case Timeline target -> timeline(target);
            case Track track -> track(Objects.requireNonNull(context.timeline()), track.getId());
            case Clip clip -> clip(Objects.requireNonNull(context.timeline()), Objects.requireNonNull(context.track()).getId(), clip.getId());
            default -> throw new IllegalArgumentException("Unknown EventContext.target() value: " + context.target().getClass().getName());
        };
    }

    private enum Kind {
        TIMELINE,
        TRACK,
        CLIP
    }
}
