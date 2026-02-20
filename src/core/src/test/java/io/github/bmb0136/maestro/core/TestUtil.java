package io.github.bmb0136.maestro.core;

import io.github.bmb0136.maestro.core.event.EventResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TestUtil {
    private TestUtil() {
    }

    public static Stream<Arguments> allCapitalization(int index, Iterable<Arguments> args) {
        List<Arguments> result = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        for (Arguments arg : args) {
            var objects = arg.get();
            char[] chars = ((String) objects[index]).toCharArray();

            int combinations = 1 << chars.length;
            for (int mask = 0; mask < combinations; mask++) {
                for (int j = 0; j < chars.length; j++) {
                    chars[j] = (mask & (1 << j)) != 0 ? Character.toUpperCase(chars[j]) : Character.toLowerCase(chars[j]);
                }

                var s = new String(chars);
                if (!seen.add(s)) {
                    continue;
                }

                Object[] newObjects = new Object[objects.length];
                System.arraycopy(objects, 0, newObjects, 0, objects.length);
                newObjects[index] = s;
                result.add(Arguments.of(newObjects));
            }
        }
        return result.stream();
    }

    public static Stream<Arguments> allMidiNotes() {
        return Stream.iterate(0, x -> x + 1).limit(128).map(Arguments::of);
    }

    public static int countIterable(Iterable<?> iterable) {
        int count = 0;
        for (var it = iterable.iterator(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> T assertOptional(Optional<T> opt) {
        Assertions.assertTrue(opt.isPresent());
        return opt.get();
    }

    public static void assertOk(EventResult result) {
        Assertions.assertTrue(result.isOk());
    }
}
