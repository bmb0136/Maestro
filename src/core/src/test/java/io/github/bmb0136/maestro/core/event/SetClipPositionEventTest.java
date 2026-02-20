package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestClip;
import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SetClipPositionEventTest {

    @Test
    public void invalidPosition_fails() {
        final float INITIAL_POS = 12.34f;
        final float NEW_POS = -1.0f;
        var data = TimelineManagerUtil.createWithClip(TestClip::new, c -> c.setPosition(INITIAL_POS));

        var result = data.manager().append(new SetClipPositionEvent(data.trackId(), data.clipId(), NEW_POS));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.INVALID_CLIP_POSITION, result);
        Assertions.assertTrue(Math.abs(INITIAL_POS - clip.getPosition()) < 1e-6f);
        Assertions.assertFalse(Math.abs(NEW_POS - clip.getPosition()) < 1e-6f);
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, 1.0f, 100000f})
    public void noOverlap_ok(float newPosition) {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);

        var result = data.manager().append(new SetClipPositionEvent(data.trackId(), data.clipId(), newPosition));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertTrue(Math.abs(newPosition - clip.getPosition()) < 1e-6f);
    }

    @ParameterizedTest
    @ValueSource(floats = {0.1f, 1.0f, 1.5f})
    public void overlap_fails(float newPosition) {
        final float INITIAL_POS = 0.0f;
        var data = TimelineManagerUtil.createWithClip(TestClip::new, c -> {
            c.setPosition(1.0f);
            c.setDuration(1.0f);
        });

        var newClip = new TestClip();
        newClip.setMutable(true);
        newClip.setPosition(INITIAL_POS);
        newClip.setDuration(1.0f);
        newClip.setMutable(false);
        TestUtil.assertOk(data.manager().append(new AddClipToTrackEvent(data.trackId(), newClip)));

        var result = data.manager().append(new SetClipPositionEvent(data.trackId(), newClip.getId(), newPosition));

        Assertions.assertEquals(EventResult.CLIP_OVERLAP, result);
        Assertions.assertTrue(Math.abs(INITIAL_POS - newClip.getPosition()) < 1e-6f);
        Assertions.assertFalse(Math.abs(newPosition - newClip.getPosition()) < 1e-6f);
    }
}
