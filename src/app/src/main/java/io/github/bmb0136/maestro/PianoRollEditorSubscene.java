package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddNoteToPianoRollClipEvent;
import io.github.bmb0136.maestro.core.event.EventResult;
import io.github.bmb0136.maestro.core.event.RemoveNoteFromPianoRollClipEvent;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PianoRollEditorSubscene extends SubScene {

    private static final double PIXELS_PER_PITCH = 20.0;
    private static final HashMap<Pitch, Double> PITCH_TO_Y = new HashMap<>();

    static {
        for (int midi = 0; midi <= 127; midi++) {
            double y = PIXELS_PER_PITCH * (127 - midi);
            PITCH_TO_Y.put(Pitch.fromMidi(midi, false), y);
            PITCH_TO_Y.put(Pitch.fromMidi(midi, true), y);
        }
    }

    private final TimelineManager manager;
    private final UUID trackId;
    private final UUID clipId;
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(120.0);
    private final SimpleObjectProperty<PianoRollClip> clip;

    @FXML
    private Region root;
    @FXML
    private AnchorPane notesPane, pianoPane;
    @FXML
    private ScrollPane notesScrollPane, pianoScrollPane;
    @FXML
    private GridPane gridLines;

    public PianoRollEditorSubscene(TimelineManager manager, UUID trackId, UUID clipId) {
        // Dummy node (can't pass null here)
        // Size doesn't matter, it will be automatically resized
        super(new Pane(), 1, 1);
        this.manager = manager;
        this.trackId = trackId;
        this.clipId = clipId;
        clip = new SimpleObjectProperty<>((PianoRollClip) manager.get().getTrack(trackId).flatMap(t -> t.getClip(clipId)).orElseThrow());
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
        var cc = new ColumnConstraints();
        rc.setPrefHeight(PIXELS_PER_PITCH);
        cc.prefWidthProperty().bind(pixelsPerBeat);
        gridLines.getRowConstraints().clear();
        gridLines.getColumnConstraints().clear();
        while (gridLines.getRowCount() < 127) {
            gridLines.getRowConstraints().add(rc);
        }
        while (gridLines.getColumnCount() < clip.get().getDuration()) {
            gridLines.getColumnConstraints().add(cc);
        }
        clip.addListener((x1, x2, newValue) -> {
            int count = (int) Math.ceil(newValue.getDuration());
            if (count < gridLines.getColumnCount()) {
                gridLines.getColumnConstraints().remove(count, gridLines.getColumnCount());
                return;
            }
            while (gridLines.getColumnCount() < count) {
                gridLines.getColumnConstraints().add(cc);
            }
        });

        // Setup notes pane size
        notesPane.prefWidthProperty().bind(pixelsPerBeat.multiply(DoubleExpression.doubleExpression(clip.map(Clip::getDuration))));
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
        RollPosition position = getPosition(e.getX(), e.getY());
        // Round position
        switch (e.getButton()) {
            case PRIMARY -> {
                position = new RollPosition(position.pitch, (int) position.position);
                var note = new Note(position.pitch, position.position, 1f, 1f);
                var result = manager.append(new AddNoteToPianoRollClipEvent(trackId, clipId, note));
                if (!result.isOk()) {
                    // TODO: show error
                    System.err.println("failed to add note");
                    return;
                }
                if (result == EventResult.NOOP) {
                    return;
                }
                Node node = createNote(note);
                notesPane.getChildren().add(node);
            }
            case SECONDARY -> {
                var toRemove = clip.get().getNote(position.pitch, position.position);
                var result = manager.append(new RemoveNoteFromPianoRollClipEvent(trackId, clipId, position.pitch, position.position));
                if (!result.isOk()) {
                    // TODO: show error
                    System.err.println("failed to remove note");
                    return;
                }
                if (result == EventResult.NOOP) {
                    return;
                }
                assert toRemove.isPresent();
                for (int i = 0; i < notesPane.getChildren().size(); i++) {
                    Node node = notesPane.getChildren().get(i);
                    var note = (Note) node.getUserData();
                    if (note.equals(toRemove.get())) {
                        notesPane.getChildren().remove(i);
                        break;
                    }
                }
            }
            default -> {
            }
        }
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

    private RollPosition getPosition(double x, double y) {
        float position = (float) (x / pixelsPerBeat.get());
        int midi = (int) Math.max(Math.min(127 - Math.floor(y / PIXELS_PER_PITCH), 127), 0);
        return new RollPosition(Pitch.fromMidi(midi, false), position);
    }

    public static PianoRollEditorSubscene create(TimelineManager manager, UUID trackId, UUID clipId) {
        var resource = Objects.requireNonNull(App.class.getResource("/PianoRoll.fxml"));
        var loader = new FXMLLoader(resource);
        try {
            var s = new PianoRollEditorSubscene(manager, trackId, clipId);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
