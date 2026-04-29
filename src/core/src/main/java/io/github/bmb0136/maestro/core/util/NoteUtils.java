package io.github.bmb0136.maestro.core.util;

import io.github.bmb0136.maestro.core.theory.Note;

import java.util.ArrayList;
import java.util.List;

public final class NoteUtils {
    private NoteUtils() {}

    public static List<Tuple2<Float, List<Note>>> groupByPosition(Iterable<Note> notes) {
        ArrayList<Tuple2<Float, List<Note>>> groups = new ArrayList<>();
        for (Note n : notes) {
            boolean foundGroup = false;

            for (var tuple : groups) {
                if (Math.abs(n.position() - tuple.first()) < 1e-6f) {
                    if (Math.abs(n.duration() - tuple.second().getFirst().duration()) >= 1e-6f) {
                        continue;
                    }
                    tuple.second().add(n);
                    foundGroup = true;
                    break;
                }
            }

            if (!foundGroup) {
                groups.add(new Tuple2<>(n.position(), new ArrayList<>(List.of(n))));
            }
        }
        return groups;
    }
}
