package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    private Pane root;
    @FXML
    private Pane ClipLine;

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
    Might be required to record the MouseEvent, Regardless of the beatPosition.
     */
    private void OnPianoRollMouseClicked(MouseEvent event) {
        root.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {

        });
    }

    private void addPianoRollClip(ActionEvent e) {
        float beatPosition = (float) (contextMenuX / pixelsPerBeat.get());
        //public static final int MOUSE_EXITED; When Mouse click is dropped
        // TODO: add clip


        try {
            URL resource = Objects.requireNonNull(App.class.getResource("/Clipper.fxml"));
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setController(root);
            Parent loads = loader.load();
            loads.getStylesheets().add("/Clipper.css");
            loads.layoutXProperty().bind(pixelsPerBeat.multiply(beatPosition));
            //
            // loads.scaleYProperty().bind(root.heightProperty());
            root.getChildren().add( loads);
            //root.getChildren().add(loads);
            System.out.println("got here");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("PianoRoll clip could not be loaded");
        }

        root.getChildren();
        System.out.println(root.getChildren());


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
        //return s;
    }
}
