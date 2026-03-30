package io.github.bmb0136.maestro.editors.clip;

import io.github.bmb0136.maestro.App;
import io.github.bmb0136.maestro.util.SpinnerUtil;
import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.event.SetChordClipBaseOctaveEvent;
import io.github.bmb0136.maestro.core.event.SetChordClipQualityEvent;
import io.github.bmb0136.maestro.core.event.SetChordClipRootNoteEvent;
import io.github.bmb0136.maestro.core.event.SetChordClipSlashNoteEvent;
import io.github.bmb0136.maestro.core.theory.ChordQuality;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.util.*;

public class ChordClipEditor extends ClipEditorSubScene<ChordClip> {

    private static final String NO_SLASH_NOTE = "None";

    @FXML
    private Region root;
    @FXML
    private ChoiceBox<Object> rootChoiceBox, qualityChoiceBox, slashNoteChoiceBox;
    @FXML
    private Label notesLabel;
    @FXML
    private Spinner<Object> baseOctaveSpinner;

    public ChordClipEditor(TimelineManager manager, UUID trackId, UUID clipId) {
        super(manager, trackId, clipId);
    }

    public static ChordClipEditor create(TimelineManager manager, UUID trackId, UUID clipId) {
        var resource = Objects.requireNonNull(App.class.getResource("/ChordClip.fxml"));
        var loader = new FXMLLoader(resource);
        try {
            var s = new ChordClipEditor(manager, trackId, clipId);
            loader.setController(s);
            s.setRoot(loader.load());
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatChordQuality(ChordQuality quality) {
        return switch (quality) {
            case MAJOR -> "Major";
            case MINOR -> "Minor";
            case AUGMENTED -> "Augmented";
            case DIMINISHED_TRIAD -> "Diminished";
            case DIMINISHED_SEVENTH -> "Diminished 7";
            case HALF_DIMINISHED -> "Half Diminished";
            case SUS2 -> "Sus 2";
            case SUS4 -> "Sus 4";
        };
    }

    private static Optional<ChordQuality> getChordQuality(String raw) {
        return switch (raw) {
            case "Major" -> Optional.of(ChordQuality.MAJOR);
            case "Minor" -> Optional.of(ChordQuality.MINOR);
            case "Augmented" -> Optional.of(ChordQuality.AUGMENTED);
            case "Diminished" -> Optional.of(ChordQuality.DIMINISHED_TRIAD);
            case "Diminished 7" -> Optional.of(ChordQuality.DIMINISHED_SEVENTH);
            case "Half Diminished" -> Optional.of(ChordQuality.HALF_DIMINISHED);
            case "Sus 2" -> Optional.of(ChordQuality.SUS2);
            case "Sus 4" -> Optional.of(ChordQuality.SUS4);
            default -> Optional.empty();
        };
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        clip.addListener(ignored -> {
            var view = clip.get().getChordBuilderView();
            rootChoiceBox.setValue(view.getRootNote().toString());
            qualityChoiceBox.setValue(formatChordQuality(view.getQuality()));
            slashNoteChoiceBox.setValue(Optional.ofNullable(view.getSlashNote()).map(PitchName::toString).orElse(NO_SLASH_NOTE));
            baseOctaveSpinner.getValueFactory().setValue(view.getBaseOctave());
            updateNotesLabel();
        });
        final var initialView = clip.get().getChordBuilderView();

        // Init root note choice box
        rootChoiceBox.getItems().clear();
        for (PitchName value : PitchName.values()) {
            rootChoiceBox.getItems().add(value.toString());
        }
        rootChoiceBox.setValue(initialView.getRootNote().toString());

        // Init chord quality choice box
        qualityChoiceBox.getItems().clear();
        for (ChordQuality value : ChordQuality.values()) {
            qualityChoiceBox.getItems().add(formatChordQuality(value));
        }
        qualityChoiceBox.setValue(formatChordQuality(initialView.getQuality()));

        // Init bass octave spinner
        baseOctaveSpinner.setValueFactory(SpinnerUtil.toObjectValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Pitch.fromMidi(0, false).octave(),
                Pitch.fromMidi(127, false).octave(),
                4)));
        baseOctaveSpinner.getValueFactory().setValue(baseOctaveSpinner.getValueFactory().getValue());

        // Init slash note choice box
        slashNoteChoiceBox.getItems().clear();
        slashNoteChoiceBox.getItems().add(NO_SLASH_NOTE);
        for (PitchName value : PitchName.values()) {
            slashNoteChoiceBox.getItems().add(value.toString());
        }
        slashNoteChoiceBox.setValue(Optional.ofNullable(initialView.getSlashNote()).map(PitchName::toString).orElse(NO_SLASH_NOTE));

        // Init notes label
        updateNotesLabel();

        // Listen for user input and append events
        rootChoiceBox.setOnAction(e -> {
            var parsed = PitchName.tryParse(rootChoiceBox.getValue().toString()).orElse(PitchName.C);
            var result = manager.append(new SetChordClipRootNoteEvent(trackId, clipId, parsed));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set chord root note: " + result, ButtonType.OK).showAndWait();
            }
        });
        qualityChoiceBox.setOnAction(e -> {
            var parsed = getChordQuality(qualityChoiceBox.getValue().toString()).orElse(ChordQuality.MAJOR);
            var result = manager.append(new SetChordClipQualityEvent(trackId, clipId, parsed));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set chord quality: " + result, ButtonType.OK).showAndWait();
            }
        });
        baseOctaveSpinner.valueProperty().addListener(ignored -> {
            Object value = baseOctaveSpinner.getValue();
            if (!(value instanceof Integer octave)) {
                throw new IllegalStateException("Chord clip editor base octave spinner had invalid value");
            }
            var result = manager.append(new SetChordClipBaseOctaveEvent(trackId, clipId, octave));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set base octave: " + result, ButtonType.OK).showAndWait();
            }
        });
        slashNoteChoiceBox.setOnAction(e -> {
            var parsed = PitchName.tryParse(slashNoteChoiceBox.getValue().toString()).orElse(null);
            var result = manager.append(new SetChordClipSlashNoteEvent(trackId, clipId, parsed));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set chord slash note: " + result, ButtonType.OK).showAndWait();
            }
        });
    }

    private void updateNotesLabel() {
        final var view = clip.get().getChordBuilderView();
        var sb = new StringBuilder();
        sb.append("Notes: ");
        var pitches = new ArrayList<Pitch>();
        for (var p : view) {
            pitches.add(p);
        }
        pitches.sort(Comparator.naturalOrder());
        for (int i = 0; i < pitches.size(); i++) {
            var p = pitches.get(i);
            sb.append(p.name());
            sb.append(p.octave());
            if (i < pitches.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" (");
        sb.append(view.getChordName());
        sb.append(")");
        notesLabel.setText(sb.toString());
    }
}
