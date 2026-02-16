package io.github.bmb0136.maestro.core.timeline;

import io.github.bmb0136.maestro.core.event.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

public class TimelineManager {
    private final int maxHistory;
    private final ArrayDeque<Event<?>> events;
    private final Timeline referencePoint;
    private Timeline current;
    private int undoOffset = 0;
    private boolean currentDirty = true;

    public TimelineManager(int maxHistory, @NotNull Timeline referencePoint) {
        this.maxHistory = maxHistory;
        this.referencePoint = referencePoint;
        this.events = new ArrayDeque<>(maxHistory);
    }

    public Timeline get() {
        if (currentDirty) {
            current = referencePoint.copy(false);
            var toApply = List.copyOf(events);
            for (int i = 0; i < undoOffset; i++) {
                toApply.removeLast();
            }
            applyEvents(current, toApply, true);
            currentDirty = false;
        }
        return current;
    }

    public EventResult append(@NotNull Event<?> event) {
        // Clear future events if we had undone any events (no branching histories)
        if (undoOffset > 0) {
            while (undoOffset-- > 0) {
                events.removeLast();
            }
            currentDirty = true;
        }

        // Reapply events up to the present
        if (currentDirty) {
            current = get();
        }

        events.addLast(event);
        var res = applyEvents(current, Collections.singletonList(event), false);
        if (!res.isOk() || res.equals(EventResult.NOOP)) {
            events.removeLast();
            return res;
        }

        while (events.size() > maxHistory) {
            applyEvents(referencePoint, Collections.singletonList(events.removeFirst()), true);
        }
        return res;
    }

    public void undo() {
        if (undoOffset >= events.size()) {
            return;
        }
        undoOffset++;
        currentDirty = true;
    }

    public void redo() {
        if (undoOffset <= 0) {
            return;
        }
        undoOffset--;
        // TODO: only apply the redone event instead of marking entire current timeline dirty
        currentDirty = true;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    private static EventResult applyEvents(Timeline timeline, Iterable<Event<?>> events, boolean checkResults) {
        var result = EventResult.OK;
        for (Event<?> event : events) {
            switch (event) {
                case ClipEvent e -> {
                    var track = timeline.getTrack(e.getTrackId());
                    if (track.isEmpty()) {
                        result = EventResult.UNKNOWN_TRACK;
                        break;
                    }
                    var target = track.get().getClip(e.getClipId());
                    if (target.isEmpty()) {
                        result = EventResult.UNKNOWN_CLIP;
                        break;
                    }
                    var context = new EventContext<>(target.get(), timeline, track.get());
                    target.get().setMutable(true);
                    result = e.apply(context);
                    target.get().setMutable(false);
                }
                case TrackEvent e -> {
                    var target = timeline.getTrack(e.getTrackId());
                    if (target.isEmpty()) {
                        result = EventResult.UNKNOWN_TRACK;
                        break;
                    }
                    var context = new EventContext<>(target.get(), timeline);
                    target.get().setMutable(true);
                    result = e.apply(context);
                    target.get().setMutable(false);
                }
                case TimelineEvent e -> {
                    var context = new EventContext<>(timeline);
                    timeline.setMutable(true);
                    result = e.apply(context);
                    timeline.setMutable(false);
                }
                // If you got this error, make sure the event extends one of the above and not Event<T> directly
                default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
            }

            if (checkResults && !result.isOk()) {
                // If this happens then there was an event that put the Timeline into an invalid state
                // The purpose of the event system is to make that impossible
                // If you do get this error, something has gone horribly wrong
                throw new IllegalStateException("Failed to apply events to Timeline. An intermediate event failed to apply");
            }
        }
        return result;
    }
}
