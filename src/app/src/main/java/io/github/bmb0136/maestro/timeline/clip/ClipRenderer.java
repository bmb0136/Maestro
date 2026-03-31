package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.NotNull;

public final class ClipRenderer {
    private ClipRenderer() {}

    public static void renderClip(@NotNull Clip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area) {
        switch (clip) {
            case PianoRollClip c -> PianoRollClipRenderer.render(c, gc, area);
            case ChordClip c -> ChordClipRenderer.render(c, gc, area);
            case ScaleClip c -> ScaleClipRenderer.render(c, gc, area);
            default -> throw new IllegalArgumentException("Unknown clip type: " + clip.getClass().getName());
        }
    }

    public static String getHeaderText(@NotNull Clip clip) {
        return switch (clip) {
            case PianoRollClip ignored -> "Piano Roll";
            case ChordClip ignored -> "Chord";
            case ScaleClip ignored -> "Scale";
            default -> throw new IllegalArgumentException("Unknown clip type: " + clip.getClass().getName());
        };
    }
}
