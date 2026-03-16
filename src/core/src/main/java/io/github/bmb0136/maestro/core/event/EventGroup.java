package io.github.bmb0136.maestro.core.event;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventGroup extends Event<Object> implements Iterable<Event<?>> {
    private final List<Event<?>> children;

    public EventGroup(Event<?>... children) {
        this(List.of(children));
    }

    public EventGroup(Iterable<Event<?>> children) {
        this.children = new ArrayList<>();
        children.forEach(this.children::add);
    }

    @Override
    public EventResult apply(@NotNull EventContext<Object> context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<Event<?>> iterator() {
        return children.iterator();
    }
}
