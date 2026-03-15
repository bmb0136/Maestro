package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EventContext<T>(T target, Timeline timeline, Track track, Clip clip) {
    public EventContext(@NotNull T target, @Nullable Timeline timeline, @Nullable Track track, @Nullable Clip clip) {
        this.target = target;
        this.timeline = timeline;
        this.track = track;
        this.clip = clip;
    }

    public EventContext(@NotNull T target, @Nullable Timeline timeline, @Nullable Track track) {
        this(target, timeline, track, null);
    }

    public EventContext(@NotNull T target, @Nullable Timeline timeline) {
        this(target, timeline, null, null);
    }

    public EventContext(@NotNull T target) {
        this(target, null, null, null);
    }

    @Override
    @NotNull
    public T target() {
        return target;
    }

    @Override
    @Nullable
    public Track track() {
        return track;
    }

    @Override
    @Nullable
    public Clip clip() {
        return clip;
    }

    @Override
    @Nullable
    public Timeline timeline() {
        return timeline;
    }
}
