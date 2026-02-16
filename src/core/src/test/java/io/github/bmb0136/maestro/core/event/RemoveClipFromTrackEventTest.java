package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestClip;
import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class RemoveClipFromTrackEventTest {
    @Test
    public void exists_ok() {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);

        var result = data.manager().append(new RemoveClipFromTrackEvent(data.trackId(), data.clipId()));

        Assertions.assertEquals(EventResult.OK, result);
        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertFalse(track.hasClip(data.clipId()));
        Assertions.assertEquals(0, TestUtil.countIterable(track));
    }

    @Test
    public void doesNotExist_noop() {
        var data = TimelineManagerUtil.createWithTrack();

        UUID clipId = UUID.randomUUID();
        var result = data.manager().append(new RemoveClipFromTrackEvent(data.trackId(), clipId));

        Assertions.assertEquals(EventResult.NOOP, result);
        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertFalse(track.hasClip(clipId));
        Assertions.assertEquals(0, TestUtil.countIterable(track));
    }
}
