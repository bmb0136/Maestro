package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddTrackToTimelineEventTest {
    @Test
    public void notDuplicate_ok() {
        TimelineManager manager = TimelineManagerUtil.createEmpty();

        Track track = new Track();
        var result = manager.append(new AddTrackToTimelineEvent(track));

        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertTrue(manager.get().hasTrack(track.getId()));
        Assertions.assertEquals(1, TestUtil.countIterable(manager.get()));
    }

    @Test
    public void duplicate_fails() {
        TimelineManager manager = TimelineManagerUtil.createEmpty();

        Track track = new Track();
        manager.append(new AddTrackToTimelineEvent(track));
        var result = manager.append(new AddTrackToTimelineEvent(track));

        Assertions.assertEquals(EventResult.TRACK_ALREADY_ON_TIMELINE, result);
        Assertions.assertTrue(manager.get().hasTrack(track.getId()));
        Assertions.assertEquals(1, TestUtil.countIterable(manager.get()));
    }
}
