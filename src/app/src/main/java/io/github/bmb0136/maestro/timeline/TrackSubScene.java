package io.github.bmb0136.maestro.timeline;

import io.github.bmb0136.maestro.App;
import io.github.bmb0136.maestro.core.event.*;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TrackSubScene extends SubScene implements AutoCloseable {

    public static final int HEIGHT = 120;

    private final TimelineManager manager;
    private final UUID trackId;
    private final BiConsumer<UUID, CallbackType> callback;
    private final AutoCloseable changeCallback;

    @FXML
    private Parent root;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField nameEditField;
    @FXML
    private Button upButton, downButton;
    private String lastName = "???";
    private final ContextMenu contextMenu = new ContextMenu();

    private TrackSubScene(TimelineManager manager, UUID trackId, BiConsumer<UUID, CallbackType> callback) {
        // Dummy node (can't pass null here)
        super(new Pane(), 240, HEIGHT);
        this.manager = manager;
        this.trackId = trackId;
        this.callback = callback;

        changeCallback = manager.registerChangeCallback(target -> {
            var timeline = target.getTimeline();

            if (target.isTimeline()) {
                upButton.setDisable(timeline.indexOf(trackId) == 0);
                downButton.setDisable(timeline.indexOf(trackId) == timeline.size() - 1);
            }

            if (!target.isTrack()) {
                return;
            }
            if (!target.getTrackId().map(id -> id.equals(trackId)).orElse(false)) {
                return;
            }
            var track = target.getTrackId().flatMap(timeline::getTrack).orElseThrow();

            lastName = track.getName();
            nameEditField.setText(lastName);
        });

        MenuItem duplicate = new MenuItem("Duplicate");
        duplicate.setOnAction(this::onDuplicate);
        contextMenu.getItems().add(duplicate);
    }

    private void onDuplicate(ActionEvent e) {
        ArrayList<Event<?>> events = new ArrayList<>();
        Timeline timeline = manager.get();

        // Copy the track, which puts it at the end of the track list
        var copy = timeline.getTrack(trackId).orElseThrow().copy(true);
        events.add(new AddTrackToTimelineEvent(copy));

        // Move it to be after to the current track
        int targetIndex = timeline.indexOf(trackId) + 1;
        for (int i = timeline.size(); i > targetIndex; i--) {
            events.add(new MoveTrackPreviousEvent(copy.getId()));
        }

        var result = manager.append(events.size() > 1 ? new EventGroup(events) : events.getFirst());
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Failed to duplicate track: " + result, ButtonType.OK).showAndWait();
        }
    }

    private void onRootClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            contextMenu.show(root, e.getScreenX(), e.getScreenY());
        }
    }

    public static TrackSubScene create(TimelineManager manager, UUID trackId, BiConsumer<UUID, CallbackType> callback) {
        URL resource = Objects.requireNonNull(App.class.getResource("/Track.fxml"));
        FXMLLoader loader = new FXMLLoader(resource);
        try {
            TrackSubScene s = new TrackSubScene(manager, trackId, callback);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
        root.setOnMouseClicked(this::onRootClicked);

        var timeline = manager.get();
        var track = timeline.getTrack(trackId).orElseThrow();
        lastName = track.getName();

        // Init name label
        nameLabel.textProperty().bind(nameEditField.textProperty());
        nameLabel.visibleProperty().bind(Bindings.not(nameEditField.visibleProperty()));

        // Init name editor
        nameEditField.setText(lastName);
        nameEditField.mouseTransparentProperty().bind(Bindings.not(nameEditField.visibleProperty()));

        nameEditField.setVisible(false);
        nameEditField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                nameEditField.setText(lastName);
                nameEditField.setVisible(false);
            }
        });

        upButton.setDisable(timeline.indexOf(trackId) == 0);
        downButton.setDisable(timeline.indexOf(trackId) == timeline.size() - 1);
    }

    @FXML
    private void onNameEdited() {
        nameEditField.setVisible(false);
        var result = manager.append(new SetTrackNameEvent(trackId, nameEditField.getText()));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Failed to update track name: " + result, ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void onNameLabelClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            nameEditField.setVisible(true);
            nameEditField.requestFocus();
        }
    }

    @FXML
    private void onDeleteButtonClicked() {
        callback.accept(trackId, CallbackType.DELETE);
    }

    @FXML
    private void onUpButtonClicked() {
        callback.accept(trackId, CallbackType.MOVE_UP);
    }

    @FXML
    private void onDownButtonClicked() {
        callback.accept(trackId, CallbackType.MOVE_DOWN);
    }

    @Override
    public void close() throws Exception {
        changeCallback.close();
    }

    public enum CallbackType {
        DELETE,
        MOVE_UP,
        MOVE_DOWN
    }
}
