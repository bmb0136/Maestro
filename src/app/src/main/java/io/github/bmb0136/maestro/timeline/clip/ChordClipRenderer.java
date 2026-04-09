package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
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
    public static void render(@NotNull ChordClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {

        gc.save();
        gc.setStroke(Color.BLACK);

        Rectangle2D new_area =
                new Rectangle2D(area.getMinX(), area.getMinY(), area.getWidth() / 2, area.getHeight()/4);

        gc.strokeRect(new_area.getMinX(), new_area.getMinY(), new_area.getWidth() , new_area.getHeight());
         updateModifierCount(gc, new_area, baseColor = Color.WHITE, clip);
         new_area = new Rectangle2D(new_area.getMinX(), (new_area.getMinY() + 4) + new_area.getHeight(), new_area.getWidth(), new_area.getHeight());
        gc.strokeRect(new_area.getMinX(), new_area.getMinY(), new_area.getWidth() , new_area.getHeight());

         NoteUpdater(gc, new_area , baseColor = Color.WHITE, clip);
        gc.restore();

    }

    private static void NoteUpdater(@NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color color, @NotNull ChordClip clip) {
    gc.clearRect(0, 0, area.getWidth(), area.getHeight());
    gc.setFill(color);

    //Grabbing the Notes for the Chord **Code Stolen from ChordClipEditor**
    Label NoteList = new Label();
    String Notes = "";
        for (Pitch pitch : clip.getChordBuilderView()) {
            Notes += pitch.name() + ", ";
        }
    //Grabbing The Major of the Chord
    Notes += "(" + clip.getChordBuilderView().getChordName() + ")";
    NoteList.setText(Notes);

        //Boundary Settings
        Font font = new Font(gc.getFont().getName(), 18);
        gc.setFont(font);
        gc.fillText(NoteList.getText(), area.getMinX(), (area.getMinY() +  (area.getMaxY() - area.getMinY() ) / 2) + 6, area.getWidth());
    }

    // case ChordClip c -> ChordClipRenderer.render(c, gc, area, baseColor);
    //Purpose: Updates the Modifier count for the Selected Clip
    //
    private static void updateModifierCount(GraphicsContext gc,Rectangle2D area, Color baseColor, ChordClip clip) {


        gc.clearRect(0, 0, area.getWidth(), area.getHeight());

        Label CountModifiers = new Label("Modifier Count: " + clip.getModifiers().size());

        CountModifiers.setFont(gc.getFont());
        CountModifiers.setMaxSize(area.getWidth(), area.getHeight());
        gc.setFill(baseColor);
        Font font = new Font(gc.getFont().getName(), Math.sqrt(area.getHeight() * area.getWidth()) / 3);
        gc.setFont(font);

        //Only needs the Width of the new Rectangle to stay within Y-Boundaries
        gc.fillText(CountModifiers.getText(), area.getMinX(), area.getMaxY() - 1, area.getWidth());






    }
}
