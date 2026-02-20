package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestClip;
import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import io.github.bmb0136.maestro.core.clip.Clip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AddClipToTrackEventTest {
    @Test
    public void noDuplicate_ok() {
        var data = TimelineManagerUtil.createWithTrack();

        Clip clip = new TestClip();
        var result = data.manager().append(new AddClipToTrackEvent(data.trackId(), clip));

        var track = data.manager().get().getTrack(data.trackId());
        assert track.isPresent();
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertTrue(track.get().hasClip(clip.getId()));
        Assertions.assertEquals(1, TestUtil.countIterable(track.get()));
    }

    @Test
    public void duplicate_fails() {
        var data = TimelineManagerUtil.createWithTrack();

        Clip clip = new TestClip();
        data.manager().append(new AddClipToTrackEvent(data.trackId(), clip));
        var result = data.manager().append(new AddClipToTrackEvent(data.trackId(), clip));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.CLIP_ALREADY_ON_TIMELINE, result);
        Assertions.assertTrue(track.hasClip(clip.getId()));
        Assertions.assertEquals(1, TestUtil.countIterable(track));
    }

    @Test
    public void endOfClipEqualsStartOfOther_ok() {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);
        TestUtil.assertOk(data.manager().append(new SetClipPositionEvent(data.trackId(), data.clipId(), 1.0f)));
        TestUtil.assertOk(data.manager().append(new SetClipDurationEvent(data.trackId(), data.clipId(), 1.0f)));

        Clip clip = new TestClip();
        clip.setMutable(true);
        clip.setPosition(0.0f);
        clip.setDuration(1.0f);
        clip.setMutable(false);

        var result = data.manager().append(new AddClipToTrackEvent(data.trackId(), clip));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertTrue(track.hasClip(clip.getId()));
        Assertions.assertEquals(2, TestUtil.countIterable(track));
    }

    @ParameterizedTest
    @ValueSource(floats = {1.0001f, 1.5f, 2.0f})
    public void overlap_fails(float duration) {
        var data = TimelineManagerUtil.createWithClip(TestClip::new);
        data.manager().append(new SetClipPositionEvent(data.trackId(), data.clipId(), 1.0f));
        data.manager().append(new SetClipDurationEvent(data.trackId(), data.clipId(), 1.0f));

        Clip clip = new TestClip();
        clip.setMutable(true);
        clip.setPosition(0.0f);
        clip.setDuration(duration);
        clip.setMutable(false);

        var result = data.manager().append(new AddClipToTrackEvent(data.trackId(), clip));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.CLIP_OVERLAP, result);
        Assertions.assertFalse(track.hasClip(clip.getId()));
        Assertions.assertEquals(1, TestUtil.countIterable(track));
    }
}
