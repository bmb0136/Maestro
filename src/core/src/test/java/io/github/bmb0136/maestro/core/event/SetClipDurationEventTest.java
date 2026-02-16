package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestClip;
import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SetClipDurationEventTest {

    @Test
    public void invalidDuration_fails() {
        final float INITIAL_DUR = 12.34f;
        final float NEW_DURATION = -1.0f;
        var data = TimelineManagerUtil.createWithClip(TestClip::new, c -> c.setDuration(INITIAL_DUR));

        var result = data.manager().append(new SetClipDurationEvent(data.trackId(), data.clipId(), NEW_DURATION));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.INVALID_CLIP_DURATION, result);
        Assertions.assertTrue(Math.abs(INITIAL_DUR - clip.getDuration()) < 1e-6f);
        Assertions.assertFalse(Math.abs(NEW_DURATION - clip.getDuration()) < 1e-6f);
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, 1.0f, 100000f})
    public void noOverlap_ok(float newDuration) {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);

        var result = data.manager().append(new SetClipDurationEvent(data.trackId(), data.clipId(), newDuration));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertTrue(Math.abs(newDuration - clip.getDuration()) < 1e-6f);
    }

    @ParameterizedTest
    @ValueSource(floats = {1.01f, 2.0f, 3.0f})
    public void overlap_fails(float newDuration) {
        final float INITIAL_DUR = 0.0f;
        var data = TimelineManagerUtil.createWithClip(TestClip::new, c -> {
            c.setPosition(1.0f);
            c.setDuration(1.0f);
        });

        var newClip = new TestClip();
        newClip.setMutable(true);
        newClip.setPosition(0.0f);
        newClip.setDuration(INITIAL_DUR);
        newClip.setMutable(false);
        TestUtil.assertOk(data.manager().append(new AddClipToTrackEvent(data.trackId(), newClip)));

        var result = data.manager().append(new SetClipDurationEvent(data.trackId(), newClip.getId(), newDuration));

        Assertions.assertEquals(EventResult.CLIP_OVERLAP, result);
        Assertions.assertTrue(Math.abs(INITIAL_DUR - newClip.getDuration()) < 1e-6f);
        Assertions.assertFalse(Math.abs(newDuration - newClip.getDuration()) < 1e-6f);
    }
}
