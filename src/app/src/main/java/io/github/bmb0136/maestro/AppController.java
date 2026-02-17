package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.AddTrackToTimelineEvent;
import io.github.bmb0136.maestro.core.event.RemoveTrackFromTimelineEvent;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.awt.*;
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
    private Parent root;
    private final TimelineManager manager = new TimelineManager(1024, new Timeline());

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
    }

    @FXML
    private void onAddTrackButtonClicked() {
        Track track = new Track();
        if (!manager.append(new AddTrackToTimelineEvent(track)).isOk()) {
            return;
        }

        var subScene = TrackSubScene.create(manager, track.getId(), this::trackCallback);
        trackList.getChildren().add(subScene);
    }

    private void trackCallback(UUID trackId, TrackSubScene.CallbackType type) {
        switch (type) {
            case DELETE -> {
                int index = manager.get().indexOf(trackId);
                if (!manager.append(new RemoveTrackFromTimelineEvent(trackId)).isOk()) {
                    return;
                }
                trackList.getChildren().remove(index);
            }
            case null, default -> throw new IllegalArgumentException();
        }
    }
}
