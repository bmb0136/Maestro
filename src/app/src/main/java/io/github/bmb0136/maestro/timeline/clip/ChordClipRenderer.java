package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
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
	 * @param clip      The clip being rendered
	 * @param gc        The {@link GraphicsContext} associated with the timeline canvas
	 * @param area      The area of the timeline canvas to draw to
	 * @param baseColor The primary accent color to render things in
	 */
	public static void render(@NotNull ChordClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {

		gc.save();

		//BackGround Change
		gc.setFill(baseColor.darker());
		gc.fillRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());

        //Sets TextColor
		Color clipTextColor = Color.WHITE;

		//Modifier Function
		updateModifierCount(gc, area, clipTextColor, clip);

		//Chord + NoteList Function
		NoteUpdater(gc, area, clipTextColor, clip);
		gc.restore();

	}
	//Grabs the NoteList assigned to Chord; grabbed through  ChordBuilderView
	/// Area - The New designated Area of the NoteList
	/// Color - Color of Text
	private static void NoteUpdater(@NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color color, @NotNull ChordClip clip) {
		gc.clearRect(0, 0, area.getWidth(), area.getHeight());
		gc.setFill(color);

		//Grabbing the Notes for the Chord
		Label NoteList = new Label();
		String Notes = "";
		for (Pitch pitch : clip.getChordBuilderView()) {
			Notes += pitch.name() + ", ";

		}
		//I'm Lazy(Gets rid of the last Comma) - Trent
		Notes = Notes.substring(0, Notes.length() - 2);

		//Grabbing The Name of the Chord
		String Chord_name = clip.getChordBuilderView().getChordName();
		NoteList.setText(Notes);

		//Boundary Settings
		Font Notelist_font = new Font(gc.getFont().getName(), 16);
		Font Chord_font = new Font(gc.getFont().getName(), 32);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		double length = (area.getMaxX() - area.getMinX()); //Due to how the canvas is rendered, using MaxX & MaxY won't lock the text in-place.
		double width = (area.getMaxY() - area.getMinY());

		//Plotting the Chord(Name) down; Middle of NoteCard
		gc.setFont(Chord_font);
		gc.fillText(Chord_name, area.getMinX() + (length / 2), area.getMinY() + (width / 2));


		//Plotting the Chord(list) down; Mid-Bottom of NoteCard
		gc.setFont(Notelist_font);
		gc.fillText(NoteList.getText(), area.getMinX() + (length / 2), area.getMinY() + (width - 15));

	}


	//Purpose: Updates the Modifier count for the Selected Clip
	/// /Area - The New designated Area of the NoteList
	/// Color - Color of Text
	private static void updateModifierCount(GraphicsContext gc, Rectangle2D area, Color color, ChordClip clip) {


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
