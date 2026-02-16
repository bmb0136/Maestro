package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class RemoveTrackFromTimelineEventTest {
    @Test
    public void exists_ok() {
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new RemoveTrackFromTimelineEvent(data.trackId()));

        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertFalse(data.manager().get().hasTrack(data.trackId()));
        Assertions.assertEquals(0, TestUtil.countIterable(data.manager().get()));
    }

    @Test
    public void doesNotExist_noop() {
        var manager = TimelineManagerUtil.createEmpty();
        UUID trackId = UUID.randomUUID();

        var result = manager.append(new RemoveTrackFromTimelineEvent(trackId));

        Assertions.assertEquals(EventResult.NOOP, result);
        Assertions.assertFalse(manager.get().hasTrack(trackId));
        Assertions.assertEquals(0, TestUtil.countIterable(manager.get()));
    }
}
