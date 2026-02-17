package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.AddTrackToTimelineEvent;
import io.github.bmb0136.maestro.core.event.RemoveTrackFromTimelineEvent;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AppController {

    @FXML
    private ScrollBar centerScrollBar;
    @FXML
    private Node centerColumn;
    @FXML
    private ScrollPane trackListScrollPane;
    @FXML
    private VBox trackList;
    @FXML
    private VBox trackClipList;
    @FXML
    private Parent root;
    @FXML
    private Label bpmLabel;
    private final TimelineManager manager = new TimelineManager(1024, new Timeline());
    private final SimpleIntegerProperty bpm = new SimpleIntegerProperty(120);

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        centerScrollBar.valueProperty().bindBidirectional(trackListScrollPane.vvalueProperty());
        trackList.getChildren().addListener((ListChangeListener<Node>) change -> {
            centerScrollBar.setVisibleAmount(trackListScrollPane.getHeight() / trackList.getHeight());
            centerScrollBar.setVisible(trackList.getHeight() > trackListScrollPane.getHeight());
        });
        centerScrollBar.visibleProperty().addListener((observableValue, oldValue, newValue) -> {
            GridPane.setRowSpan(centerColumn, newValue ? 2 : 3);
        });
        centerScrollBar.setVisible(false);

        bpmLabel.textProperty().bind(bpm.map(value -> "BPM: " + value));
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
        trackClipList.getChildren().add(TrackClipsSubScene.create(manager, track.getId()));
    }

    private void trackCallback(UUID trackId, TrackSubScene.CallbackType type) {
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
