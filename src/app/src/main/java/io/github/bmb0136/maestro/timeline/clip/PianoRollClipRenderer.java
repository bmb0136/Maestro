package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.theory.Note;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PianoRollClipRenderer {
	/**
	 * Render the inside of the given clip onto the timeline
	 *
	 * @param clip      The clip being rendered
	 * @param gc        The {@link GraphicsContext} associated with the timeline canvas
	 * @param area      The area of the timeline canvas to draw to
	 * @param baseColor The primary accent color to render things in
	 */
	public static void render(@NotNull PianoRollClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
	gc.save();
		PianoUpdater(clip, gc, area, baseColor = Color.BLACK);
	gc.restore();
	}

	public static void PianoUpdater(@NotNull PianoRollClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
		gc.clearRect(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());
		gc.setFill(baseColor);
		//gc.fillRect(area.getMinX(), area.getMinY(), 60, 34);
		System.out.println(area.getMinX() + " Min " + area.getMinY());
		System.out.println(area.getMaxX() + " Max " + area.getMaxY());
		//gc.fillRect(area.getMinX() + 60,area.getMinY() + 0, 60, 34);
		int minMidi = 9999;
		int maxMidi = -9999;

		ArrayList<Note> First_position = new ArrayList<Note>();
		ArrayList<Note> Second_position = new ArrayList<Note>();
		ArrayList<Note> Third_position = new ArrayList<Note>();
		ArrayList<Note> Four_position = new ArrayList<Note>();

		for (Note note : clip) {
			System.out.println("Midi Score: " + note.pitch().toMidi());
			if (maxMidi < note.pitch().toMidi()) {
				maxMidi = note.pitch().toMidi();
			}
			if (minMidi > note.pitch().toMidi()) {
				minMidi = note.pitch().toMidi();
			}
			 switch((int) note.position()){
				 case 0:
					 First_position.add(note);
					 break;
				 case 1:
					 Second_position.add(note);
					 break;
				 case 2:
					 Third_position.add(note);
					 break;
				 case 3:
					 Four_position.add(note);
					 break;
				 default:
					 System.out.println(note.position() + "Wrong");
					 break;
			 }
		}
		System.out.println(First_position.size() + " " +  First_position);
		//System.out.println(Second_position.size() + " " +  Second_position);
		//System.out.println(Third_position.size() + " " +  Third_position);
		//System.out.println(Four_position.size() + " " +  Four_position);
		ArrayList<ArrayList<Note>> holder = new ArrayList<>();  //LMAO - Trent
		holder.add(First_position);
		holder.add(Second_position);
		holder.add(Third_position);
		holder.add(Four_position);


		if (minMidi == maxMidi) {
			minMidi -= 10;
			maxMidi += 10;
		}


		System.out.println("maxMidi " + maxMidi);
		System.out.println("minMidi " + minMidi);

		double scale = maxMidi - minMidi;



		gc.setFill(baseColor);
		System.out.println("Size " + holder.size());

		for (ArrayList<Note> notes : holder) {

			int averageMidi = 0;
			for (Note note : notes) {
				averageMidi += note.pitch().toMidi();
			}
			if (!notes.isEmpty()) {
				averageMidi /= notes.size();
				System.out.println(notes.size() + " Average:  " + averageMidi);


				double y = ((double) (averageMidi - minMidi) / (scale + 1) * (area.getMinY() - area.getMaxY())) + area.getMaxY();
				double height = area.getHeight() / (scale + 1);

				double x = notes.getFirst().position() / clip.getDuration() * area.getWidth();
				System.out.println("x: " + x);
				System.out.println("y: " + y);
				double width = notes.getFirst().duration() / clip.getDuration() * area.getWidth();
				System.out.println("width: " + width);
				System.out.println("height: " + height);
				gc.fillRect(area.getMinX() + x,  y, width, height);
			}
		}
		/*
		for (Note note : clip) {

			double y = ((double) (note.pitch().toMidi() - minMidi) / (scale + 1) * (area.getMinY() - area.getMaxY())) + area.getMaxY();
			double height = area.getHeight() / (scale + 1 );

			double x = note.position() / clip.getDuration() * area.getWidth();
			double width = note.duration() / clip.getDuration() * area.getWidth();
			gc.fillRect(x, y, width, height);
		}

		 */
	}
        /* Brandon's Code
        if (minMidi == maxMidi) {
            minMidi -= 10;
            maxMidi += 10;
        }
        double y = ((double)(note.pitch().toMidi() - minMidi) / (maxMidi - minMidi + 1) * (area.getMinY() - area.getMaxY())) + area.getMaxY();
        double height = area.getHeight() / (maxMidi - minMidi + 1);
        double x = note.position() / clip.getDuration() * area.getWidth();
        double width = note.duration() / clip.getDuration() * area.getWidth();
    }

       */

}
