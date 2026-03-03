package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

public class TimelineRenderer {
    private final TimelineManager manager;
    private final Canvas canvas;
    private final SimpleDoubleProperty pixelsPerBeat;
    // The position of the left side of the timeline view in beats
    private final SimpleFloatProperty scrollXBeats = new SimpleFloatProperty(0);
    // The position of the top side of the timeline view in tracks (fraction represents between)
    private final SimpleDoubleProperty scrollYTracks = new SimpleDoubleProperty(0);
    // The position of the playback head in beats
    private final SimpleFloatProperty playbackHeadXBeats = new SimpleFloatProperty(0);
    private final SimpleObjectProperty<Bounds> scrollbarBounds = new SimpleObjectProperty<>();

    public TimelineRenderer(@NotNull TimelineManager manager, @NotNull Canvas canvas, @NotNull SimpleDoubleProperty pixelsPerBeat) {
        this.manager = manager;
        this.canvas = canvas;
        this.pixelsPerBeat = pixelsPerBeat;

        canvas.widthProperty().addListener(ignored -> draw());
        canvas.heightProperty().addListener(ignored -> draw());
        scrollbarBoundsProperty().addListener(ignored -> draw());
        scrollXBeats.addListener(ignored -> draw());
        scrollYTracks.addListener(ignored -> draw());
        playbackHeadXBeats.addListener(ignored -> draw());

        canvas.setOnScroll(this::onScroll);
        canvas.setOnMouseClicked(this::onClick);

        // TODO: context menu
    }

    public ObjectProperty<Bounds> scrollbarBoundsProperty() {
        return scrollbarBounds;
    }

    public void draw() {
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawMarkers(gc);

        drawClips(gc);

        gc.setStroke(Color.RED);
        var playbackHeadX = beatsToLocalX(playbackHeadXBeats.get());
        gc.strokeLine(playbackHeadX, 0, playbackHeadX, canvas.getHeight());
    }

    private void drawClips(GraphicsContext gc) {
        // TODO
    }

    private void drawMarkers(GraphicsContext gc) {
        // Find first beat after scroll pos
        int firstBeat = (int) Math.ceil(localXToBeats(0));
        var x = beatsToLocalX(firstBeat);
        while (x < canvas.getWidth()) {
            gc.setFill(Color.WHITE);
            // Given position refers to bottom left of text
            gc.fillText(String.valueOf(firstBeat), x, gc.getFont().getSize());

            gc.setStroke(Color.gray(1.0, 0.5));
            gc.strokeLine(x, 0, x, canvas.getHeight());

            x += pixelsPerBeat.get();
            firstBeat++;
        }
    }

    private void onScroll(ScrollEvent e) {
        scrollXBeats.set((float)Math.max(0, ((e.getDeltaX() / e.getMultiplierX() * -0.25f) + scrollXBeats.get())));
    }

    private void onClick(MouseEvent e) {
        if (e.getY() < scrollbarBounds.get().getMinY()) {
            if (e.getButton() == MouseButton.PRIMARY) {
                playbackHeadXBeats.set(localXToBeats(e.getX()));
            }
        } else if (e.getY() > scrollbarBounds.get().getMaxY()) {
            System.out.println("bottom part");
        }
    }

    private double beatsToLocalX(float beats) {
        return (beats - scrollXBeats.get()) * pixelsPerBeat.get();
    }

    private float localXToBeats(double x) {
        return (float) (x / pixelsPerBeat.get()) + scrollXBeats.get();
    }
}
