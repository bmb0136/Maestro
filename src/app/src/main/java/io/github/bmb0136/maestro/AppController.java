package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.AddTrackToTimelineEvent;
import io.github.bmb0136.maestro.core.event.RemoveTrackFromTimelineEvent;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
    private ScrollPane trackClipListScrollPane;
    @FXML
    private VBox trackList;
    @FXML
    private VBox trackClipList;
    @FXML
    private Region root;
    @FXML
    private Label bpmLabel;
    @FXML
    private Line playbackHead;
    @FXML
    private ScrollPane timeMarkerScrollPane;
    @FXML
    private TilePane timeMarkerList;
    @FXML
    private TitledPane editorPane;
    private final TimelineManager manager = new TimelineManager(1024, new Timeline());
    private final SimpleIntegerProperty bpm = new SimpleIntegerProperty(120);
    // TODO: Bind zooming to this property (decrease to zoom in, increase to zoom out)
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(60.0);

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        // Sync scroll bars with relevant scroll panes
        trackScrollBar.valueProperty().bindBidirectional(trackListScrollPane.vvalueProperty());
        trackScrollBar.valueProperty().bindBidirectional(trackClipListScrollPane.vvalueProperty());
        timelineScrollBar.valueProperty().bindBidirectional(trackClipListScrollPane.hvalueProperty());
        timelineScrollBar.valueProperty().bindBidirectional(timeMarkerScrollPane.hvalueProperty());

        // Auto-resize track scroll bar handle based on track count
        trackList.getChildren().addListener((ListChangeListener<Node>) change -> {
            trackScrollBar.setVisibleAmount(trackListScrollPane.getHeight() / trackList.getHeight());
            trackScrollBar.setVisible(trackList.getHeight() > trackListScrollPane.getHeight());
        });
        trackScrollBar.setVisible(false);
        // TODO: Auto-resize timeline scroll bar handle based on length of timeline
        timelineScrollBar.setVisibleAmount(0.1);
        //timelineScrollBar.setVisible(false);

        // Make column span over scroll bar's space when scroll bar is invisible
        // Can't always make this the case since the scroll bar is semi-transparent
        trackScrollBar.visibleProperty().addListener((observableValue, oldValue, newValue) -> {
            GridPane.setRowSpan(trackColumn, newValue ? 2 : 3);
        });
        timelineScrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
            GridPane.setColumnSpan(timelineColumn, newValue ? 2 : 3);
        });

        bpmLabel.textProperty().bind(bpm.map(value -> "BPM: " + value));

        // Make playback head span entire window
        playbackHead.endYProperty().bind(root.heightProperty());

        // Resize based on pixels per beat
        timeMarkerList.prefTileWidthProperty().bind(pixelsPerBeat);
        // TODO: propagate pixelsPerBeat to subscenes

        updateTimeMarkers();

        // Prevent incorrect scrolling
        timeMarkerScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaY()) > 1e-6) {
                e.consume();
            }
        });
        trackListScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaX()) > 1e-6) {
                e.consume();
            }
        });
    }

    private void updateTimeMarkers() {
        int numMarkers = (int)Math.max(MIN_TIMELINE_LENGTH, manager.get().getDuration());
        timeMarkerList.setPrefColumns(numMarkers);
        for (int i = timeMarkerList.getChildren().size(); i < numMarkers; i++) {
            var pane = new Pane();
            pane.setMouseTransparent(true);

            Label label = new Label();
            // TODO: add measure number
            label.setText(String.valueOf(i + 1));
            label.setPadding(new Insets(8, 0, 4, 0));
            label.layoutXProperty().bind(label.widthProperty().multiply(-0.5));
            pane.getChildren().add(label);

            Line line = new Line();
            line.setStartX(0);
            line.startYProperty().bind(label.heightProperty());
            line.setEndX(0);
            line.endYProperty().bind(root.heightProperty());
            line.setStroke(Color.GRAY);
            pane.getChildren().add(line);

            timeMarkerList.getChildren().add(pane);
        }
    }

    @FXML
    private void onTimeMarkerListClicked(MouseEvent e) {
        playbackHead.setLayoutX(e.getX());
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
        SubScene trackClips = TrackClipsSubScene.create(manager, track.getId());
        trackClips.widthProperty().bind(timeMarkerList.widthProperty());
        trackClipList.getChildren().add(trackClips);
    }

    private void trackCallback(UUID trackId, TrackCallbackType type) {
        switch (type) {
            case DELETE -> {
                int index = manager.get().indexOf(trackId);
                if (!manager.append(new RemoveTrackFromTimelineEvent(trackId)).isOk()) {
                    return;
                }
                trackList.getChildren().remove(index);
                trackClipList.getChildren().remove(index);
            }
            case null, default -> throw new IllegalArgumentException();
        }
    }
}
