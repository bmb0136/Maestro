package io.github.bmb0136.maestro;

import java.util.Comparator;

public class NoteEventComparator implements Comparator<NoteEvent> {
    @Override
    public int compare(NoteEvent A, NoteEvent B) {
        if (A.getTimeExecution() > B.getTimeExecution()) {
            return 1;
        } else if (A.getTimeExecution() < B.getTimeExecution()) {
            return -1;
        }
        return 0;
    }
}
