package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.ClipFactory;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddClipToTrackEvent;
import io.github.bmb0136.maestro.core.event.RemoveClipFromTrackEvent;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class TimelineRenderer {
    private final TimelineManager manager;
    private final Canvas canvas;
    private final SimpleDoubleProperty pixelsPerBeat;
    // Position of the top-left corner of the view
    // X uses beats and Y uses tracks (fractional tracks means the view is scrolled between tracks)
    private final SimpleFloatProperty scrollXBeats = new SimpleFloatProperty();
    private final SimpleDoubleProperty scrollYTracks = new SimpleDoubleProperty();
    // The position of the playback head in beats
    private final SimpleFloatProperty playbackHeadXBeats = new SimpleFloatProperty();
    // Needed to discriminate between the area above and below the timeline scrollbar (see main window)
    // Also used for some calculations
    private final SimpleObjectProperty<Bounds> scrollbarBounds = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty timelineSize = new SimpleIntegerProperty();
    private final SimpleDoubleProperty maxScrollY = new SimpleDoubleProperty();
    // Needed to convert tracks/beats <-> percent (scrollbar in main window uses 0-1 range)
    private final SimpleDoubleProperty scrollYPercent = new SimpleDoubleProperty();
    private final SimpleDoubleProperty scrollXPercent = new SimpleDoubleProperty();
    // Context menus
    private final ContextMenu trackContextMenu = new ContextMenu();
    private final ContextMenu clipContextMenu = new ContextMenu();
    private double contextMenuX, contextMenuY;
    // Selection related fields
    private final SimpleObjectProperty<UUID> selectedClip = new SimpleObjectProperty<>(null);
    // Cache of visible elements
    private final HashMap<UUID, Rectangle2D> visibleClips = new HashMap<>();
    private final HashSet<UUID> visibleTracks = new HashSet<>();

    public TimelineRenderer(@NotNull TimelineManager manager, @NotNull Canvas canvas, @NotNull SimpleDoubleProperty pixelsPerBeat) {
        this.manager = manager;
        this.canvas = canvas;
        this.pixelsPerBeat = pixelsPerBeat;

        // This object lives as long as the application, no need to close callback
        //noinspection resource
        manager.registerChangeCallback(target -> {
            if (target.isTimeline()) {
                timelineSize.set(target.getTimeline().size());
            }
            // If an on-screen track/clip changes, redraw
            if (target.getTrackId().map(visibleTracks::contains).orElse(false)
                    || target.getClipId().map(visibleClips::containsKey).orElse(false)) {
                draw();
            }
        });

        canvas.widthProperty().addListener(ignored -> draw());
        canvas.heightProperty().addListener(ignored -> draw());
        scrollbarBoundsProperty().addListener(ignored -> draw());
        scrollXBeats.addListener(ignored -> draw());
        scrollYTracks.addListener(ignored -> draw());
        playbackHeadXBeats.addListener(ignored -> draw());
        selectedClip.addListener(ignored -> draw());

        scrollYPercent.addListener((ignored1, ignored2, newValue) -> scrollYTracks.set(newValue.doubleValue() * maxScrollY.get()));
        scrollYTracks.addListener((ignored1, ignored2, newValue) -> scrollYPercent.set(newValue.doubleValue() / maxScrollY.get()));

        var canvasHeightTracks = localYToTracks(canvas.heightProperty()).subtract(localYToTracks(DoubleExpression.doubleExpression(scrollbarBounds.map(Bounds::getMaxY))));
        maxScrollY.bind(Bindings.max(0, timelineSize.subtract(canvasHeightTracks)));
        // Clamp view automatically
        maxScrollY.addListener((ignored1, ignored2, newValue) -> scrollYTracks.set(Math.min(scrollYTracks.get(), newValue.doubleValue())));

        canvas.setOnScroll(this::onScroll);
        canvas.setOnMouseClicked(this::onClick);

        // Setup context menus
        Menu addClipMenu = new Menu("Add Clip");
        addMenuItem(addClipMenu.getItems(), "Piano Roll", e -> rootContextMenuOnAddClipHandler(e, PianoRollClip::create));
        trackContextMenu.getItems().add(addClipMenu);

        addMenuItem(clipContextMenu.getItems(), "Delete", this::clipContextMenuOnDeleteHandler);
    }

    private static void addMenuItem(ObservableList<MenuItem> menuItems, String text, EventHandler<ActionEvent> onAction) {
        var item = new MenuItem();
        item.setText(text);
        item.setOnAction(onAction);
        menuItems.add(item);
    }

    private void clipContextMenuOnDeleteHandler(ActionEvent e) {
        if (!(clipContextMenu.getUserData() instanceof UUID clipId)) {
            throw new IllegalStateException("TimelineRenderer.clipContextMenu: missing clip ID in getUserData()");
        }
        manager.get().getTrackForClip(clipId).ifPresent(trackId -> {
            var result = manager.append(new RemoveClipFromTrackEvent(trackId, clipId));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete clip: " + result, ButtonType.OK).showAndWait();
            }
        });
    }

    private void rootContextMenuOnAddClipHandler(ActionEvent e, ClipFactory<?> factory) {
        float position = localXToBeats(contextMenuX);
        float duration = 4;
        var track = manager.get().getTrack((int) localYToTracks(contextMenuY));
        var result = manager.append(new AddClipToTrackEvent(track.getId(), factory.create(position, duration)));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Failed to add clip: " + result, ButtonType.OK).showAndWait();
        }
    }

    public ObjectProperty<Bounds> scrollbarBoundsProperty() {
        return scrollbarBounds;
    }

    public DoubleProperty scrollYProperty() {
        return scrollYPercent;
    }

    public void draw() {
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setGlobalAlpha(1);

        gc.save();
        drawMarkers(gc);
        gc.restore();

        gc.save();
        drawClips(gc);
        gc.restore();

        gc.setStroke(Color.RED);
        var playbackHeadX = beatsToLocalX(playbackHeadXBeats.get());
        gc.strokeLine(playbackHeadX, 0, playbackHeadX, canvas.getHeight());
    }

    private void drawClips(GraphicsContext gc) {
        var bounds = scrollbarBoundsProperty().get();
        gc.beginPath();
        gc.rect(0, bounds.getMaxY(), canvas.getWidth(), canvas.getHeight() - bounds.getMaxY());
        gc.clip();

        visibleClips.clear();
        visibleTracks.clear();

        gc.setFill(Color.BLUE);
        int trackIndex = 0;
        for (var iterator = manager.get().iterator(); iterator.hasNext(); trackIndex++) {
            var track = iterator.next();
            if (tracksToLocalY(trackIndex + 1) < bounds.getMaxY()) {
                continue;
            }

            visibleTracks.add(track.getId());

            // Draw line for each track
            gc.setStroke(Color.gray(1.0, 0.5));
            gc.setLineWidth(1);
            double trackLineY = tracksToLocalY(trackIndex + 1);
            gc.strokeLine(0, trackLineY, canvas.getWidth(), trackLineY);

            for (var clip : track) {
                double startX = beatsToLocalX(clip.getPosition());
                double endX = beatsToLocalX(clip.getPosition() + clip.getDuration());

                // Skip if outside view
                if (endX < 0 || startX > canvas.getWidth()) {
                    continue;
                }

                var rect = new Rectangle2D(startX, tracksToLocalY(trackIndex), endX - startX, TrackSubScene.HEIGHT);
                visibleClips.put(clip.getId(), rect);
                gc.fillRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());

                if (clip.getId().equals(selectedClip.get())) {
                    gc.setStroke(Color.RED);
                    gc.setLineWidth(3);
                    gc.strokeRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
                }
            }
        }
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
        scrollXBeats.set((float) Math.max(0, (e.getDeltaX() / e.getMultiplierX() * -0.25f) + scrollXBeats.get()));
        scrollYTracks.set((float) Math.max(0, Math.min(maxScrollY.get(), (e.getDeltaY() / e.getMultiplierY() * -0.1f) + scrollYTracks.get())));
    }

    private void onClick(MouseEvent e) {
        if (e.getY() < scrollbarBounds.get().getMinY()) {
            if (e.getButton() == MouseButton.PRIMARY) {
                playbackHeadXBeats.set(localXToBeats(e.getX()));
            }
        } else if (e.getY() > scrollbarBounds.get().getMaxY()) {
            switch (e.getButton()) {
                case PRIMARY -> getClipAt(e.getX(), e.getY()).ifPresentOrElse(id -> {
                    selectedClip.set(id);
                    if (e.getClickCount() == 2) {
                        // TODO: call AppController::setupEditorFor somehow
                        System.out.println("open editor for clip " + id);
                    }
                }, () -> selectedClip.set(null));
                case SECONDARY -> {
                    clipContextMenu.hide();
                    trackContextMenu.hide();

                    contextMenuX = e.getX();
                    contextMenuY = e.getY();

                    // TODO: maybe add another context menu for this?
                    if (localYToTracks(contextMenuY) >= timelineSize.get()) {
                        return;
                    }

                    getClipAt(contextMenuX, contextMenuY).ifPresentOrElse(id -> {
                        clipContextMenu.setUserData(id);
                        clipContextMenu.show(canvas, e.getScreenX(), e.getScreenY());
                    }, () -> trackContextMenu.show(canvas, e.getScreenX(), e.getScreenY()));
                }
            }
        }
    }

    private Optional<UUID> getClipAt(double x, double y) {
        for (var entry : visibleClips.entrySet()) {
            if (entry.getValue().contains(x, y)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private double beatsToLocalX(float beats) {
        return (beats - scrollXBeats.get()) * pixelsPerBeat.get();
    }

    private float localXToBeats(double x) {
        return (float) (x / pixelsPerBeat.get()) + scrollXBeats.get();
    }

    private double tracksToLocalY(double tracks) {
        return ((tracks - scrollYTracks.get()) * TrackSubScene.HEIGHT) + scrollbarBoundsProperty().get().getMaxY();
    }

    private double localYToTracks(double y) {
        return ((y - scrollbarBounds.get().getMaxY()) / TrackSubScene.HEIGHT) + scrollYTracks.get();
    }

    private DoubleExpression localYToTracks(DoubleExpression y) {
        return y.subtract(DoubleExpression.doubleExpression(scrollbarBounds.map(Bounds::getMaxY))).divide(TrackSubScene.HEIGHT).add(scrollYTracks);
    }
}
