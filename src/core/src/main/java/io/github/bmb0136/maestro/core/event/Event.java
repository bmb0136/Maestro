package io.github.bmb0136.maestro.core.event;

import org.jetbrains.annotations.NotNull;

public abstract class Event<T> {
    public abstract EventResult apply(@NotNull EventContext<T> context);
}

