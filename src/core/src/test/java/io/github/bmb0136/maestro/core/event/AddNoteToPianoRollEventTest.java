package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AddNoteToPianoRollEventTest {
    @Test
    public void valid_ok() {
        var data = TimelineManagerUtil.createWithClip(PianoRollClip::new, c -> c.setDuration(4.0f));
        var note = new Note(Pitch.fromMidi(60, false), 0.0f, 1.0f);

        var result = data.manager().append(new AddNoteToPianoRollClipEvent(data.trackId(), data.clipId(), note));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertEquals(1, TestUtil.countIterable(clip));
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.0f, 5.0f})
    public void outside_fails(float notePosition) {
        var data = TimelineManagerUtil.createWithClip(PianoRollClip::new, c -> c.setDuration(4.0f));
        var note = new Note(Pitch.fromMidi(60, false), notePosition, 1.0f);

        var result = data.manager().append(new AddNoteToPianoRollClipEvent(data.trackId(), data.clipId(), note));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.NOTE_OUTSIDE_CLIP, result);
        Assertions.assertEquals(0, TestUtil.countIterable(clip));
    }

    @Test
    public void duplicate_noop() {
        var data = TimelineManagerUtil.createWithClip(PianoRollClip::new, c -> c.setDuration(4.0f));
        var note = new Note(Pitch.fromMidi(60, false), 0.0f, 1.0f);
        AddNoteToPianoRollClipEvent event = new AddNoteToPianoRollClipEvent(data.trackId(), data.clipId(), note);
        TestUtil.assertOk(data.manager().append(event));

        var result = data.manager().append(event);

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        var clip = TestUtil.assertOptional(track.getClip(data.clipId()));
        Assertions.assertEquals(EventResult.NOOP, result);
        Assertions.assertEquals(1, TestUtil.countIterable(clip));
    }
}
