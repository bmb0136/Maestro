package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

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


         paintAreaChord(gc, area, baseColor = Color.BLACK);
        // updateModifierCount(gc, clip, area);
        //gc.fillRect(area.getWidth() / 2, area.getHeight() / 2, area.getWidth(), area.getHeight());
        //Grab Modifiers clip.getModifiers();
        //Grab Notes
    }
    // case ChordClip c -> ChordClipRenderer.render(c, gc, area, baseColor);

    private static void updateModifierCount(GraphicsContext gc, ChordClip clip, Rectangle2D area) {
        gc.clearRect(0, 0, area.getWidth(), area.getHeight());
        Label CountModifiers = new Label(clip.getModifiers().size() + "");
        gc.setFill(Color.RED);

        gc.fillText(CountModifiers.getText(), area.getWidth()-4, area.getMinY() + gc.getFont().getSize() * 3);
        //CountModifiers.setLayoutX(area.getWidth());
        //CountModifiers.setLayoutY(area.getHeight());
    }

    private static void paintAreaChord(GraphicsContext gc,Rectangle2D area, Color baseColor){
        gc.setFill(baseColor);
        System.out.print(area);
        // gc.fillRect(area.getMinX(), area.getMinY(), area.getWidth() / 2 , area.getHeight() / 4);
        Rectangle2D area2 =
                new Rectangle2D(area.getMinX(), area.getMinY(), area.getWidth() / 3, area.getHeight() / 5);
        for(int i = 0; i < 4; i++){
            gc.fillRect(area2.getMinX(), area2.getMinY(), area2.getWidth(), area2.getHeight());

            area2 = new Rectangle2D(area2.getMinX(), (area2.getMinY() + 4) + area2.getHeight(), area2.getWidth(), area2.getHeight());
        }
        //gc.fillRect(area2.getMinX(), area2.getMinY(), area2.getWidth(), area2.getHeight());
        //gc.fillRect(area.getWidth(), area.getHeight(), area.getWidth() /2, area.getHeight());
        // updateModifierCount(
        //gc.fillRoundRect(area.getWidth()/2, area.getWidth(), area.getWidth(), area.getHeight(), 20, 20);
        Label CountModifiers = new Label();

    }
}
