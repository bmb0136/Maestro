package io.github.bmb0136.maestro.core;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TimelineManagerUtil {
    private TimelineManagerUtil() {}

    public static TimelineManager createEmpty() {
        return new TimelineManager(1024, new Timeline());
    }

    public static TrackData createWithTrack() {
        Timeline tl = new Timeline();
        tl.setMutable(true);
        Track track = new Track();
        tl.addTrack(track);
        tl.setMutable(false);
        return new TrackData(new TimelineManager(1024, tl), track.getId());
    }

    public static <T extends Clip> ClipData createWithClip(Supplier<T> factory) {
        return createWithClip(factory, ignored -> {});
    }

    public static <T extends Clip> ClipData createWithClip(Supplier<T> factory, Consumer<T> mutate) {
        Timeline tl = new Timeline();
        Track track = new Track();
        var clip = factory.get();

        tl.setMutable(true);
        tl.addTrack(track);
        tl.setMutable(false);

        track.setMutable(true);
        track.addClip(clip);
        track.setMutable(false);

        clip.setMutable(true);
        mutate.accept(clip);
        clip.setMutable(false);
        return new ClipData(new TimelineManager(1024, tl), track.getId(), clip.getId());
    }

    public record TrackData(TimelineManager manager, UUID trackId) {}
    public record ClipData(TimelineManager manager, UUID trackId, UUID clipId) {}
}
