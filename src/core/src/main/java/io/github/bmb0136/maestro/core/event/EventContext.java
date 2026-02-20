package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EventContext<T>(T target, Timeline timeline, Track track) {
    public EventContext(@NotNull T target, @Nullable Timeline timeline, @Nullable Track track) {
        this.target = target;
        this.timeline = timeline;
        this.track = track;
    }

    public EventContext(@NotNull T target, @Nullable Timeline timeline) {
        this(target, timeline, null);
    }

    public EventContext(@NotNull T target) {
        this(target, null, null);
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
    public Timeline timeline() {
        return timeline;
    }
}
