package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ScaleClip;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import io.github.bmb0136.maestro.core.util.StringUtils;

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
    pitchUpdater(clip,gc,area,textColor);
        gc.restore();

    }
    private static void  pitchUpdater(ScaleClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
        gc.clearRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());
        gc.setFill(baseColor);
        Font font = new Font(gc.getFont().getName(), 18);
        gc.setFont(font);
        //String Lines
        String Pitchstuff = "";

        //Root Pitch
        Pitchstuff += clip.getRootPitch() + " ";
        String temp = StringUtils.upperSnakeCaseToTitleCase(clip.getScale().name());
        Pitchstuff += temp + "\n";

        //Degrees
        temp = clip.getMinDegree() + "-"
           + clip.getMaxDegree() + " ";
        Pitchstuff += temp;

        //Mode
        temp = clip.getMode().name().toLowerCase().substring(0,1).toUpperCase() +
                clip.getMode().name().toLowerCase().substring(1).toLowerCase();
        Pitchstuff += temp + "\n";

        //Location
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double length = (area.getMaxX() - area.getMinX());
        double width = (area.getMaxY() - area.getMinY());

        gc.fillText(Pitchstuff, area.getMinX() + (length / 2), (area.getMinY() + (width / 2)));



    }

}
