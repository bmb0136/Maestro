package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddTrackToTimelineEvent;
import io.github.bmb0136.maestro.core.event.RemoveTrackFromTimelineEvent;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AppController {

    private static final float MIN_TIMELINE_LENGTH = 32.0f;

    @FXML
    private ScrollBar trackScrollBar, timelineScrollBar;
    @FXML
    private Node trackColumn, timelineColumn;
    @FXML
    private ScrollPane trackListScrollPane;
    @FXML
    private VBox trackList;
    @FXML
    private Region root;
    @FXML
    private Label bpmLabel;
    @FXML
    private TitledPane editorPane;
    @FXML
    private Canvas timelineCanvas;
    @FXML
    private Region timelineParent;
    private final TimelineManager manager = new TimelineManager(1024, new Timeline());
    private final SimpleIntegerProperty bpm = new SimpleIntegerProperty(120);
    // TODO: Bind zooming to this property (decrease to zoom in, increase to zoom out)
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(60.0);
    private TimelineRenderer renderer;

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        // Init timeline renderer
        renderer = new TimelineRenderer(manager, timelineCanvas, pixelsPerBeat);
        renderer.scrollbarBoundsProperty().bind(timelineScrollBar.boundsInParentProperty());
        timelineCanvas.widthProperty().bind(timelineParent.widthProperty());
        timelineCanvas.heightProperty().bind(root.heightProperty());

        // Sync scroll bars with relevant scroll panes
        trackScrollBar.valueProperty().bindBidirectional(trackListScrollPane.vvalueProperty());

        // Auto-resize track scroll bar handle based on track count
        trackList.getChildren().addListener((ListChangeListener<Node>) change -> {
            trackScrollBar.setVisibleAmount(trackListScrollPane.getHeight() / trackList.getHeight());
            trackScrollBar.setVisible(trackList.getHeight() > trackListScrollPane.getHeight());
        });

        // Make column span over scroll bar's space when scroll bar is invisible
        // Can't always make this the case since the scroll bar is semi-transparent
        trackScrollBar.visibleProperty().addListener((observableValue, oldValue, newValue) -> {
            GridPane.setRowSpan(trackColumn, newValue ? 2 : 3);
        });
        timelineScrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
            GridPane.setColumnSpan(timelineColumn, newValue ? 2 : 3);
        });

        trackScrollBar.setVisible(false);
        // TODO: Auto-resize timeline scroll bar handle based on length of timeline
        timelineScrollBar.setVisibleAmount(0.1);
        //timelineScrollBar.setVisible(false);

        bpmLabel.textProperty().bind(bpm.map(value -> "BPM: " + value));

        // TODO: propagate pixelsPerBeat to subscenes

        // Prevent incorrect scrolling
        trackListScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaX()) > 1e-6) {
                e.consume();
            }
        });

        editorPane.prefWidthProperty().bind(timelineCanvas.widthProperty());

        // TODO: remove this
        addTrack(new Track());
    }

    private void setupEditorFor(UUID trackId, @NotNull Clip clip) {
        SubScene scene;
        switch (clip) {
            case PianoRollClip c -> scene = PianoRollEditorSubScene.create(manager, trackId, c.getId());
            default -> throw new IllegalArgumentException("Unknown clip type: " + clip.getClass().getName());
        }

        scene.widthProperty().bind(editorPane.prefWidthProperty());
        scene.heightProperty().bind(root.heightProperty().multiply(0.5));
        editorPane.setContent(scene);
    }

    @FXML
    private void onAddTrackButtonClicked() {
        addTrack(new Track());
    }


    @FXML
    private void onBpmScrolled(ScrollEvent event) {
        if (Math.abs(event.getDeltaY()) < 1e-6) {
            return;
        }
        int delta = event.getDeltaY() >= 0 ? 1 : -1;
        bpm.set(bpm.get() + delta);
    }

    private double bpmDragStartY;
    private int bpmDragStartValue;
    @FXML
    private void onBpmDragged(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            bpmDragStartY = event.getScreenY();
            bpmDragStartValue = bpm.get();
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            bpm.set((int) (bpmDragStartValue - (0.25 * (event.getScreenY() - bpmDragStartY))));
        }
    }

    private void addTrack(@NotNull Track track) {
        if (!manager.append(new AddTrackToTimelineEvent(track)).isOk()) {
            return;
        }

        trackList.getChildren().add(TrackSubScene.create(manager, track.getId(), this::trackCallback));
        // TODO: update timeline
    }

    private void trackCallback(UUID trackId, TrackCallbackType type) {
        switch (type) {
            case DELETE -> {
                int index = manager.get().indexOf(trackId);
                if (!manager.append(new RemoveTrackFromTimelineEvent(trackId)).isOk()) {
                    return;
                }
                trackList.getChildren().remove(index);
                // TODO: update timeline
            }
            case null, default -> throw new IllegalArgumentException();
        }
    }
}
