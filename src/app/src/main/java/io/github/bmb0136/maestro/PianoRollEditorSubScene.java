package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddNoteToPianoRollClipEvent;
import io.github.bmb0136.maestro.core.event.RemoveNoteFromPianoRollClipEvent;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.UUID;

public class PianoRollEditorSubScene extends ClipEditorSubScene<PianoRollClip> {

    private static final double PIXELS_PER_PITCH = 20.0;
    private static final HashMap<Pitch, Double> PITCH_TO_Y = new HashMap<>();

    static {
        for (int midi = 0; midi <= 127; midi++) {
            double y = PIXELS_PER_PITCH * (127 - midi);
            PITCH_TO_Y.put(Pitch.fromMidi(midi, false), y);
            PITCH_TO_Y.put(Pitch.fromMidi(midi, true), y);
        }
    }

    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(120.0);
    private final SimpleDoubleProperty gridDivisions = new SimpleDoubleProperty(1);

    @FXML
    private Region root;
    @FXML
    private AnchorPane notesPane, pianoPane;
    @FXML
    private ScrollPane notesScrollPane, pianoScrollPane;
    @FXML
    private GridPane gridLines;

    public PianoRollEditorSubScene(TimelineManager manager, UUID trackId, UUID clipId) {
        super(manager, trackId, clipId);
    }

    public static PianoRollEditorSubScene create(TimelineManager manager, UUID trackId, UUID clipId) {
        var resource = Objects.requireNonNull(App.class.getResource("/PianoRoll.fxml"));
        var loader = new FXMLLoader(resource);
        try {
            var s = new PianoRollEditorSubScene(manager, trackId, clipId);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");
        root.getStylesheets().add("/PianoRoll.css");
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());

        // Load notes from clip
        for (Note note : clip.get()) {
            notesPane.getChildren().add(createNote(note));
        }
        clip.addListener(ignored -> {
            notesPane.getChildren().clear();
            for (Note note : clip.get()) {
                notesPane.getChildren().add(createNote(note));
            }
        });

        // Init piano display
        for (int midi = 127; midi >= 0; midi--) {
            var pitch = Pitch.fromMidi(midi, false);
            Rectangle r = new Rectangle();
            r.widthProperty().bind(pianoPane.widthProperty());
            r.setHeight(PIXELS_PER_PITCH);
            r.setFill(pitch.name().isNatural() ? Color.WHITE : Color.BLACK);
            r.setStroke(Color.gray(0.5));
            r.setLayoutY(PITCH_TO_Y.get(pitch));
            pianoPane.getChildren().add(r);

            if (pitch.name().equals(PitchName.C)) {
                Label label = new Label();
                label.setText(pitch.name().toString() + pitch.octave());
                label.setStyle("-fx-text-fill: BLACK;");
                label.setTextAlignment(TextAlignment.RIGHT);
                label.prefWidthProperty().bind(pianoPane.widthProperty());
                label.setLayoutY(r.getLayoutY());
                pianoPane.getChildren().add(label);
            }
        }

        // Init grid lines
        gridLines.prefWidthProperty().bind(notesPane.widthProperty());
        gridLines.prefHeightProperty().bind(notesPane.heightProperty());
        var rc = new RowConstraints();
        rc.setPrefHeight(PIXELS_PER_PITCH);
        gridLines.getRowConstraints().clear();
        while (gridLines.getRowCount() < 127) {
            gridLines.getRowConstraints().add(rc);
        }

        // Add updaters for grid lines
        clip.addListener((x1, x2, newValue) -> updateGridLines(newValue.getDuration()));
        gridDivisions.addListener(x -> updateGridLines(clip.get().getDuration()));
        updateGridLines(clip.get().getDuration());

        // Setup notes pane size
        notesPane.prefWidthProperty().bind(pixelsPerBeat.multiply(DoubleExpression.doubleExpression(clip.map(c -> Math.ceil(c.getDuration())))));
        notesPane.prefHeightProperty().bind(pianoPane.heightProperty());

        // Sync scroll panes
        notesScrollPane.vvalueProperty().bindBidirectional(pianoScrollPane.vvalueProperty());

        // Disable invalid scrolling
        pianoPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaX()) >= 1e-6) {
                e.consume();
            }
        });
    }

    @FXML
    private void onNotesPaneClicked(MouseEvent e) {
        RollPosition position = mouseToRollPosition(e.getX(), e.getY());
        // Round position
        switch (e.getButton()) {
            case PRIMARY -> {
                var notePos = (float) (Math.floor(position.position * gridDivisions.get()) / gridDivisions.get());
                position = new RollPosition(position.pitch, notePos);
                var note = new Note(position.pitch, position.position, 1f / gridDivisions.floatValue(), 1f);
                var result = manager.append(new AddNoteToPianoRollClipEvent(trackId, clipId, note));
                if (!result.isOk()) {
                    new Alert(Alert.AlertType.ERROR, "Failed to add note: " + result, ButtonType.OK).showAndWait();
                }
            }
            case SECONDARY -> {
                var result = manager.append(new RemoveNoteFromPianoRollClipEvent(trackId, clipId, position.pitch, position.position));
                if (!result.isOk()) {
                    new Alert(Alert.AlertType.ERROR, "Failed to remove note: " + result, ButtonType.OK).showAndWait();
                }
            }
            default -> {
            }
        }
    }

    @FXML
    private void onNotesPaneKeyPressed(KeyEvent e) {
        OptionalDouble divisions = switch (e.getText()) {
            case "7" -> OptionalDouble.of(0.25);  // Whole
            case "6" -> OptionalDouble.of(0.5);   // Half
            case "5" -> OptionalDouble.of(1);     // Quarter
            case "4" -> OptionalDouble.of(2);     // Eighth
            case "3" -> OptionalDouble.of(4);     // Sixteenth
            default -> OptionalDouble.empty();
        };
        divisions.ifPresent(gridDivisions::set);
    }

    private Node createNote(Note note) {
        var r = new Rectangle();
        r.widthProperty().bind(pixelsPerBeat.multiply(note.duration()));
        r.setHeight(PIXELS_PER_PITCH);
        r.xProperty().bind(pixelsPerBeat.multiply(note.position()));
        r.setY(PITCH_TO_Y.get(note.pitch()));
        r.getStyleClass().add("note");
        r.setUserData(note);
        return r;
    }

    private RollPosition mouseToRollPosition(double x, double y) {
        float position = (float) (x / pixelsPerBeat.get());
        int midi = (int) Math.max(Math.min(127 - Math.floor(y / PIXELS_PER_PITCH), 127), 0);
        return new RollPosition(Pitch.fromMidi(midi, false), position);
    }

    private void updateGridLines(float duration) {
        var cc = new ColumnConstraints();
        int count = (int) Math.ceil(gridDivisions.get() * duration);
        cc.prefWidthProperty().bind(pixelsPerBeat.divide(gridDivisions));
        gridLines.getColumnConstraints().clear();
        while (gridLines.getColumnCount() < count) {
            gridLines.getColumnConstraints().add(cc);
        }
    }

    private record RollPosition(Pitch pitch, float position) {
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RollPosition(Pitch pitch1, float position1))) {
                return false;
            }
            return pitch.equals(pitch1) && Math.abs(position - position1) < 1e-6f;
        }
    }
}
