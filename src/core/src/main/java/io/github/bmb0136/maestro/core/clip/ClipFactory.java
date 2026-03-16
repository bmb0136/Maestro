package io.github.bmb0136.maestro.core.clip;

@FunctionalInterface
public interface ClipFactory<T extends Clip> {
    T create(float position, float duration);
}
