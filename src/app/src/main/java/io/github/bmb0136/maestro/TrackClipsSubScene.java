package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddClipToTrackEvent;
import io.github.bmb0136.maestro.core.event.RemoveClipFromTrackEvent;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class TrackClipsSubScene extends SubScene {
    private final TimelineManager manager;
    private final UUID trackId;
    private final TrackClipCallback callback;
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(60.0); // TODO: propagate from AppController

    private final ContextMenu rootContextMenu = new ContextMenu();
    private final ContextMenu clipContextMenu = new ContextMenu();

    @FXML
    private Pane root;

    private double contextMenuX, contextMenuY;
    @Nullable
    private Node lastNode = null;

    private TrackClipsSubScene(TimelineManager manager, UUID trackId, TrackClipCallback callback) {
        // Dummy node (can't pass null here)
        super(new Pane(), 500, 120);
        this.manager = manager;
        this.trackId = trackId;
        this.callback = callback;

        Menu addMenu = new Menu();
        addMenu.setText("Add");
        addMenuItem(addMenu.getItems(), "Piano Roll", this::addPianoRollClip);
        rootContextMenu.getItems().add(addMenu);

        addMenuItem(clipContextMenu.getItems(), "Delete", this::deleteClip);
    }

    private void deleteClip(ActionEvent e) {
        assert lastNode != null;
        var clipId = (UUID) lastNode.getUserData();
        var result = manager.append(new RemoveClipFromTrackEvent(trackId, clipId));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Error: " + result, ButtonType.OK).showAndWait();
            return;
        }
        root.getChildren().remove(lastNode);
    }

    private void addPianoRollClip(ActionEvent e) {
        float beatPosition = (float) (contextMenuX / pixelsPerBeat.get());
        PianoRollClip clip = PianoRollClip.create(beatPosition, 4f);
        var pane = addClip(clip);
        if (pane == null) {
            return;
        }
        // TODO: render notes inside of clip
    }

    @Nullable
    private Pane addClip(Clip clip) {
        var result = manager.append(new AddClipToTrackEvent(trackId, clip));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Error: " + result, ButtonType.OK).showAndWait();
            return null;
        }
        var pane = new Pane();
        pane.prefHeightProperty().bind(root.heightProperty());
        pane.layoutXProperty().bind(pixelsPerBeat.multiply(clip.getPosition()));
        pane.prefWidthProperty().bind(pixelsPerBeat.multiply(clip.getDuration()));
        pane.setBackground(Background.fill(Color.BLUE));
        pane.setUserData(clip.getId());
        root.getChildren().add(pane);
        return pane;
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());
    }

    @FXML
    private void onRootClicked(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY -> {
                if (!(e.getTarget() instanceof Node node && node.getUserData() instanceof UUID clipId)) {
                    return;
                }
                if (e.getClickCount() == 2) {
                    callback.run(trackId, clipId, CallbackType.OPEN_EDITOR);
                }
            }
            case SECONDARY -> {
                lastNode = null;
                if (e.getTarget() == root) {
                    rootContextMenu.show(root, e.getScreenX(), e.getScreenY());
                } else if (e.getTarget() instanceof Node node) {
                    lastNode = node;
                    clipContextMenu.show(node, e.getScreenX(), e.getScreenY());
                }

                contextMenuX = e.getX();
                contextMenuY = e.getY();
            }
            case null, default -> {
            }
        }
    }

    public static SubScene create(TimelineManager manager, UUID trackId, TrackClipCallback callback) {
        URL resource = Objects.requireNonNull(App.class.getResource("/TrackClips.fxml"));
        FXMLLoader loader = new FXMLLoader(resource);
        try {
            var s = new TrackClipsSubScene(manager, trackId, callback);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addMenuItem(ObservableList<MenuItem> items, String name, EventHandler<ActionEvent> onAction) {
        MenuItem item = new MenuItem();
        item.setText(name);
        item.setOnAction(onAction);
        items.add(item);
    }

    @FunctionalInterface
    public interface TrackClipCallback {
        void run(UUID trackId, UUID clipId, CallbackType type);
    }

    public enum CallbackType {
        OPEN_EDITOR;
    }
}
