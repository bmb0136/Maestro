package io.github.bmb0136.maestro.timeline.clip;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.theory.Note;
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

		PianoUpdater(clip, gc, area, baseColor = Color.BLACK);

	}

	private static void PianoUpdater(@NotNull PianoRollClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color color) {
	int minMidi = -9999;
	int maxMidi = 9999;

	for (Note note : clip){
		if(maxMidi < note.pitch().toMidi()){
			maxMidi = note.pitch().toMidi();
		}
		else if (minMidi > note.pitch().toMidi()){
			minMidi = note.pitch().toMidi();

		}
	}
	if (minMidi == maxMidi) {
		minMidi -= 10;
		maxMidi += 10;
	}
	gc.setFill(color);
	double scale = maxMidi - minMidi;

	//gc.fillRect(0, 0, area.getWidth()* clip.getDuration(), area.getHeight());
	for (Note note : clip){
		double y = ((double) (note.pitch().toMidi() - minMidi) / (scale + 1) * (area.getMinY() - area.getMaxY())) + area.getMaxY();
		double height = area.getHeight() / (scale + 1);

		double x = note.position() / clip.getDuration() * area.getWidth();
		double width = note.duration() / clip.getDuration() * area.getWidth();
		gc.fillRect(x, y, height, width);
	}
	}
}
