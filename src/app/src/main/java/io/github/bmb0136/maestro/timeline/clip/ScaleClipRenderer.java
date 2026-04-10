package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ScaleClip;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

public class ScaleClipRenderer {
    /**
     * Render the inside of the given clip onto the timeline
     *
     * @param clip The clip being rendered
     * @param gc The {@link GraphicsContext} associated with the timeline canvas
     * @param area The area of the timeline canvas to draw to
     * @param baseColor The primary accent color to render things in
     */
    public static void render(@NotNull ScaleClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
    gc.save();
    gc.setFill(baseColor.darker());
    gc.fillRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());
    Color textColor = Color.WHITE;
        UpdateModifierCount(clip, gc, area, textColor);
        gc.restore();
    }
    private static void UpdateModifierCount(ScaleClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color color) {
        gc.clearRect(0, 0, area.getWidth(), area.getHeight());
        gc.setFill(color);

        //Label for Modifier
        Label CountModifiers = new Label( + clip.getModifiers().size() + "M");

        CountModifiers.setFont(gc.getFont());
        Font font = new Font(gc.getFont().getName(), 12 + (Math.sqrt(area.getHeight() / 4)));
        gc.setFont(font);

        //Only needs the Width of the new Rectangle to stay within Y-Boundaries
        gc.fillText(CountModifiers.getText(), area.getMaxX() - 25, area.getMinY(), area.getWidth());
    }
}
