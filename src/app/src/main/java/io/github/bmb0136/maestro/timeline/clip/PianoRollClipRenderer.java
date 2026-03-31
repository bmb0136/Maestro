package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

public class PianoRollClipRenderer {
    /**
     * Render the inside of the given clip onto the timeline
     *
     * @param clip The clip being rendered
     * @param gc The {@link GraphicsContext} associated with the timeline canvas
     * @param area The area of the timeline canvas to draw to
     * @param baseColor The primary accent color to render things in
     */
    public static void render(@NotNull PianoRollClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
    }
}
