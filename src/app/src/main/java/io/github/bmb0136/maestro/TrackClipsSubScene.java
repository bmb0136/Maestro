package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class TrackClipsSubScene extends SubScene {
    private final TimelineManager manager;
    private final UUID trackId;

    @FXML
    private Parent root;

    private TrackClipsSubScene(TimelineManager manager, UUID trackId) {
        // Dummy node (can't pass null here)
        super(new Pane(), 500, 120);
        this.manager = manager;
        this.trackId = trackId;
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
    }

    public static SubScene create(TimelineManager manager, UUID trackId) {
        URL resource = Objects.requireNonNull(App.class.getResource("/TrackClips.fxml"));
        FXMLLoader loader = new FXMLLoader(resource);
        try {
            var s = new TrackClipsSubScene(manager, trackId);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
