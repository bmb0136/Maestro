package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestClip;
import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseEventContextTest {
    @Test
    public void checkTimelineEvent() {
        var manager = TimelineManagerUtil.createEmpty();

        var result = manager.append(new TimelineEvent() {
            @Override
            public EventResult apply(@NotNull EventContext<Timeline> context) {
                Assertions.assertNotNull(context.target());
                Assertions.assertTrue(context.target().isMutable());

                Assertions.assertNull(context.timeline());

                Assertions.assertNull(context.track());
                return EventResult.OK;
            }
        });

        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertFalse(manager.get().isMutable());
    }

    @Test
    public void checkTrackEvent() {
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new TrackEvent(data.trackId()) {
            @Override
            public EventResult apply(@NotNull EventContext<Track> context) {
                Assertions.assertNotNull(context.target());
                Assertions.assertTrue(context.target().isMutable());

                Assertions.assertNotNull(context.timeline());
                Assertions.assertFalse(context.timeline().isMutable());

                Assertions.assertNull(context.track());
                return EventResult.OK;
            }
        });

        Assertions.assertEquals(EventResult.OK, result);
        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertFalse(track.isMutable());
    }

    @Test
    public void checkClipEvent() {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);

        var result = data.manager().append(new ClipEvent(data.trackId(), data.clipId()) {
            @Override
            public EventResult apply(@NotNull EventContext<Clip> context) {
                Assertions.assertNotNull(context.target());
                Assertions.assertTrue(context.target().isMutable());

                Assertions.assertNotNull(context.timeline());
                Assertions.assertFalse(context.timeline().isMutable());

                Assertions.assertNotNull(context.track());
                Assertions.assertFalse(context.track().isMutable());
                return EventResult.OK;
            }
        });

        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertFalse(data.manager().get().isMutable());
        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertFalse(track.isMutable());
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertFalse(clip.isMutable());
    }
}
