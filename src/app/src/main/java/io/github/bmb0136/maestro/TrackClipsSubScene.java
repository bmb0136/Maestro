package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class TrackClipsSubScene extends SubScene {
    private final TimelineManager manager;
    private final UUID trackId;
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(60.0); // TODO: propagate from AppController

    private final ContextMenu contextMenu = new ContextMenu();

    @FXML
    private AnchorPane root;

    private double contextMenuX, contextMenuY;

    private TrackClipsSubScene(TimelineManager manager, UUID trackId) {
        // Dummy node (can't pass null here)
        super(new Pane(), 500, 120);
        this.manager = manager;
        this.trackId = trackId;

        Menu addMenu = new Menu();
        addMenu.setText("Add");
        MenuItem addPianoRoll = new MenuItem();
        addPianoRoll.setText("Piano Roll");
        addPianoRoll.setOnAction(this::addPianoRollClip);
        addMenu.getItems().add(addPianoRoll);
        contextMenu.getItems().add(addMenu);
    }
    /*


     */
    private void OnPianoRollMouseClicked(MouseEvent event) {
        root.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

        });
    }
    private void addPianoRollClip(ActionEvent e) {
        float beatPosition = (float) (contextMenuX / pixelsPerBeat.get());
        //public static final int MOUSE_EXITED; When Mouse click is dropped
        // TODO: add clip
        PianoRollClip Clip = new PianoRollClip();
        Pane pane = new Pane();
        pane.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(pane);
        //pane.widthProperty().bind(root.heightProperty());
        //pane.bind(root.heightProperty());
        //Clip.setPosition(beatPosition);


        //pane.setLocation((int)beatPosition,(int) beatPosition);
        //System.out.println(beatPosition);

        /*
        var result = manager.append(...);
        if (!result.isOK()) {
        new Alert(Alert.AlertType.ERROR, "Error: " + result, ButtonType.OK).showAndWait();
        return;
        }

         */
        Button test_button = new Button("Test");
        System.out.println(root.getChildren());
        //root.getChildren().add(pane);
        //root.getChildren().add(test_button);
        //root.getChildren().add();

    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());
    }

    @FXML
    private void onRootClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.SECONDARY) {
            return;
        }

        contextMenu.show(root, e.getScreenX(), e.getScreenY());
        contextMenuX = e.getX();
        contextMenuY = e.getY();
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
