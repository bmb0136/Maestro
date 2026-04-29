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
	 * @param clip      The clip being rendered
	 * @param gc        The {@link GraphicsContext} associated with the timeline canvas
	 * @param area      The area of the timeline canvas to draw to
	 * @param baseColor The primary accent color to render things in
	 */
	public static void render(@NotNull PianoRollClip clip, @NotNull GraphicsContext gc, @NotNull Rectangle2D area, @NotNull Color baseColor) {
		if (!clip.iterator().hasNext()) {
			return;
		}

		int minMidi = Integer.MAX_VALUE;
		int maxMidi = Integer.MIN_VALUE;
		for (Note note : clip) {
			var midi = note.pitch().toMidi();

			if (midi > maxMidi) {
				maxMidi = midi;
			}

			if (midi < minMidi) {
				minMidi = midi;
			}
		}
		final int MIN_SCALE = 10;
		while (maxMidi - minMidi < MIN_SCALE) {
			maxMidi++;
			minMidi--;
		}
		minMidi--;

		gc.setFill(baseColor);
		for (Note note : clip) {
			double y = ((double) (note.pitch().toMidi() - minMidi) / (maxMidi - minMidi) * (area.getMinY() - area.getMaxY())) + area.getMaxY();
			double height = area.getHeight() / (maxMidi - minMidi);

			double x = (note.position() / clip.getDuration() * area.getWidth()) + area.getMinX();
			double width = note.duration() / clip.getDuration() * area.getWidth();
			gc.fillRect(x, y, width, height);
		}
	}
}
