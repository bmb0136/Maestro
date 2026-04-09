package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ChordClipRenderer {
    /**
     * Render the inside of the given clip onto the timeline
     *
     * @param clip The clip being rendered
     * @param gc The {@link GraphicsContext} associated with the timeline canvas
     * @param area The area of the timeline canvas to draw to
     * @param baseColor The primary accent color to render things in
     */
    // final ChordClip clip;
    // final GraphicsContext gc;
    // REctangle2D area;
    // color baseColor;
    public static void render(@NotNull ChordClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {

        gc.save();
         updateModifierCount(gc, area, baseColor = Color.BLACK, clip);
        gc.restore();
        //gc.fillRect(area.getWidth() / 2, area.getHeight() / 2, area.getWidth(), area.getHeight());
        //Grab Modifiers clip.getModifiers();
        //Grab Notes
    }
    // case ChordClip c -> ChordClipRenderer.render(c, gc, area, baseColor);
    //Purpose: Updates the Modifier count for the Selected Clip
    //
    private static void updateModifierCount(GraphicsContext gc,Rectangle2D area, Color baseColor, ChordClip clip) {


        gc.clearRect(0, 0, area.getWidth(), area.getHeight());
        gc.setStroke(baseColor);
        Rectangle2D area2 =
                new Rectangle2D(area.getMinX(), area.getMinY(), area.getWidth() / 2, area.getHeight()/4);
            gc.strokeRect(area.getMinX(), area.getMinY(), area2.getWidth() , area2.getHeight());
        Label CountModifiers = new Label("Modifier Count: " + clip.getModifiers().size() + "");
        CountModifiers.setFont(gc.getFont());
        gc.setFill(Color.WHITE);
        Font font = new Font(gc.getFont().getName(), 18);
        gc.setFont(font);
        gc.fillText(CountModifiers.getText(), area2.getMinX(), area2.getMaxY(), 100);

        //gc.setTextAlign(TextAlignment.RIGHT);
        //gc.fillText("Modifiers", area2.getMaxX(), area2.getMaxY());

        /*
        for(int i = 0; i < 4; i++){
            gc.fillRect(area2.getMinX(), area2.getMinY(), area2.getWidth(), area2.getHeight());
            gc.save();
            updateModifierCount(gc, clip, area2);
            gc.restore();

            area2 = new Rectangle2D(area2.getMinX(), (area2.getMinY() + 4) + area2.getHeight(), area2.getWidth(), area2.getHeight());
        }
         */



    }
}
