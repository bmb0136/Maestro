package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.SetTrackNameEvent;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TrackSubScene extends SubScene {
    private final TimelineManager manager;
    private final UUID trackId;
    private final BiConsumer<UUID, CallbackType> callback;

    @FXML
    private Label nameLabel;
    @FXML
    private TextField nameEditField;
    @FXML
    private Parent root;

    private TrackSubScene(TimelineManager manager, UUID trackId, BiConsumer<UUID, CallbackType> callback) {
        super(new Pane(), 240, 120);
        this.manager = manager;
        this.trackId = trackId;
        this.callback = callback;
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
        manager.get().getTrack(trackId).ifPresent(t -> nameEditField.setText(t.getName()));
        nameLabel.textProperty().bind(nameEditField.textProperty());
        nameLabel.visibleProperty().bind(Bindings.not(nameEditField.visibleProperty()));
        nameEditField.mouseTransparentProperty().bind(Bindings.not(nameEditField.visibleProperty()));
        nameEditField.setVisible(false);
    }

    @FXML
    private void onNameEdited() {
        if (!manager.append(new SetTrackNameEvent(trackId, nameEditField.getText())).isOk()) {
            return;
        }
        var name = manager.get().getTrack(trackId).orElseThrow().getName();
        nameEditField.setText(name);
        nameEditField.setVisible(false);
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

    public enum CallbackType {
        DELETE
    }
}
