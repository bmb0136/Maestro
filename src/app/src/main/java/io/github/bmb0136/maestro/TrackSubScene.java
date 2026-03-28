package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.SetTrackNameEvent;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TrackSubScene extends SubScene implements AutoCloseable {

    public static final int HEIGHT = 120;

    private final TimelineManager manager;
    private final UUID trackId;
    private final BiConsumer<UUID, TrackCallbackType> callback;
    private final AutoCloseable changeCallback;

    @FXML
    private Parent root;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField nameEditField;
    private String lastName = "???";

    private TrackSubScene(TimelineManager manager, UUID trackId, BiConsumer<UUID, TrackCallbackType> callback) {
        // Dummy node (can't pass null here)
        super(new Pane(), 240, HEIGHT);
        this.manager = manager;
        this.trackId = trackId;
        this.callback = callback;

        changeCallback = manager.registerChangeCallback(target -> {
            if (!target.isTrack()) {
                return;
            }
            if (target.getTrackId().map(id -> id.equals(trackId)).orElse(false)) {
                return;
            }
            var track = target.getTrackId().flatMap(target.getTimeline()::getTrack).orElseThrow();

            lastName = track.getName();
            nameEditField.setText(lastName);
        });
    }

    public static TrackSubScene create(TimelineManager manager, UUID trackId, BiConsumer<UUID, TrackCallbackType> callback) {
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

        var track = manager.get().getTrack(trackId).orElseThrow();
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
        callback.accept(trackId, TrackCallbackType.DELETE);
    }

    @Override
    public void close() throws Exception {
        changeCallback.close();
    }
}
