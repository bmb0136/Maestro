package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.Objects;
import java.util.UUID;

public class PianoRollEditorSubscene extends SubScene {

    private static final double PIXELS_PER_PITCH = 20.0;

    private final TimelineManager manager;
    private final UUID clipId;

    @FXML
    private Parent root;
    @FXML
    private AnchorPane notesPane;

    public PianoRollEditorSubscene(TimelineManager manager, UUID clipId) {
        // Dummy node (can't pass null here)
        // Size doesn't matter, it will be automatically resized
        super(new Pane(), 1, 1);
        this.manager = manager;
        this.clipId = clipId;
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        // TODO: load notes from clip

        // Init notes display
        int numNotes = 0;
        for (int midi = 127; midi >= 0; midi--) {
            var pitch = Pitch.fromMidi(midi, false);
            Rectangle r = new Rectangle();
            r.widthProperty().bind(notesPane.widthProperty());
            r.setHeight(PIXELS_PER_PITCH);
            r.setFill(pitch.name().isNatural() ? Color.WHITE : Color.BLACK);
            r.setStroke(Color.gray(0.5));
            r.setLayoutY(PIXELS_PER_PITCH * numNotes++);
            notesPane.getChildren().add(r);

            if (pitch.name().equals(PitchName.C)) {
                Label label = new Label();
                label.setText(pitch.name().toString() + pitch.octave());
                label.setStyle("-fx-text-fill: BLACK;");
                label.setTextAlignment(TextAlignment.RIGHT);
                label.prefWidthProperty().bind(notesPane.widthProperty());
                label.setLayoutY(r.getLayoutY());
                notesPane.getChildren().add(label);
            }
        }
    }

    private RollPosition getPosition(double x, double y) {
        throw new RuntimeException("TODO");
    }

    public static PianoRollEditorSubscene create(TimelineManager manager, UUID clipId) {
        var resource = Objects.requireNonNull(App.class.getResource("/PianoRoll.fxml"));
        var loader = new FXMLLoader(resource);
        try {
            var s = new PianoRollEditorSubscene(manager, clipId);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private record RollPosition(Pitch pitch, float position) {
    }
}
