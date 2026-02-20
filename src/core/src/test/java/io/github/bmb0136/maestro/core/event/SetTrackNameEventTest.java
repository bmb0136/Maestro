package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.TestUtil;
import io.github.bmb0136.maestro.core.TimelineManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

public class SetTrackNameEventTest {
    @Test
    public void null_nop() {
        final String NAME = null;
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new SetTrackNameEvent(data.trackId(), NAME));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.NOOP, result);
        Assertions.assertNotEquals(NAME, track.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    ", "  \n  ", " \t "})
    public void emptyOrWhitespace_noop(String name) {
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new SetTrackNameEvent(data.trackId(), name));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.NOOP, result);
        Assertions.assertNotEquals(name, track.getName());
    }

    @Test
    public void valid_ok() {
        final String NAME = "Hello";
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new SetTrackNameEvent(data.trackId(), NAME));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertEquals(NAME, track.getName());
    }

    @ParameterizedTest
    @MethodSource("validWithWhitespace_args")
    public void validWithWhitespace_ok(String name, String expected) {
        var data = TimelineManagerUtil.createWithTrack();

        var result = data.manager().append(new SetTrackNameEvent(data.trackId(), name));

        var track = TestUtil.assertOptional(data.manager().get().getTrack(data.trackId()));
        Assertions.assertEquals(EventResult.OK, result);
        Assertions.assertEquals(expected, track.getName());
    }

    private static Stream<Arguments> validWithWhitespace_args() {
        return Stream.of(
                Arguments.of( "   Spaces   ", "Spaces"),
                Arguments.of( "\t\tTabs\t\t", "Tabs"),
                Arguments.of("\nNew Lines\n", "New Lines")
        );
    }
}
