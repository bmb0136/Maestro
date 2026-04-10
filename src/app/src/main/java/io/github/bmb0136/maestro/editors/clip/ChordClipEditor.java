package io.github.bmb0136.maestro.editors.clip;

import io.github.bmb0136.maestro.App;
import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.event.*;
import io.github.bmb0136.maestro.core.theory.Accidental;
import io.github.bmb0136.maestro.core.theory.ChordQuality;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.util.BiHashMap;
import io.github.bmb0136.maestro.core.util.Tuple2;
import io.github.bmb0136.maestro.util.SpinnerUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.*;

public class ChordClipEditor extends ClipEditorSubScene<ChordClip> {

    private static final String NO_SLASH_NOTE = "None";
    private static final String NO_ALTERATION = "Select to Add";
    private static final BiHashMap<String, Tuple2<Accidental, Integer>> ALTERATIONS_MAP = new BiHashMap<>();

    static {
        StringBuilder sb = new StringBuilder();
        for (int degree = 1; degree <= 15; degree++) {
            if (degree == 1 || degree % 2 == 0) {
                continue;
            }
            for (var accidental : Accidental.values()) {
                sb.setLength(0);
                sb.append(switch (accidental) {
                    case NATURAL -> "";
                    case SHARP -> "#";
                    case FLAT -> "b";
                });
                sb.append(degree);
                ALTERATIONS_MAP.add(sb.toString(), new Tuple2<>(accidental, degree));
            }
        }
    }

    @FXML
    private Region root, optionsList;
    @FXML
    private ChoiceBox<Object> rootChoiceBox, qualityChoiceBox, slashNoteChoiceBox, alterationChoiceBox;
    @FXML
    private Label notesLabel;
    @FXML
    private Spinner<Object> baseOctaveSpinner;
    @FXML
    private FlowPane alterationsList;

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

        optionsList.prefWidthProperty().bind(root.widthProperty());

        clip.addListener(ignored -> {
            var view = clip.get().getChordBuilderView();
            rootChoiceBox.setValue(view.getRootNote().toString());
            qualityChoiceBox.setValue(formatChordQuality(view.getQuality()));
            slashNoteChoiceBox.setValue(Optional.ofNullable(view.getSlashNote()).map(PitchName::toString).orElse(NO_SLASH_NOTE));
            baseOctaveSpinner.getValueFactory().setValue(view.getBaseOctave());
            updateNotesLabel();
            updateAlterationsList();
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

        // Init alterations choice box
        alterationChoiceBox.getItems().clear();
        alterationChoiceBox.getItems().add(NO_ALTERATION);
        ALTERATIONS_MAP.values2().stream().sorted(Comparator.comparingInt(Tuple2<Accidental, Integer>::second).thenComparingInt( t -> switch (t.first()) {
            case FLAT -> -1;
            case NATURAL -> 0;
            case SHARP -> 1;
        })).map(ALTERATIONS_MAP::get2).forEachOrdered(alterationChoiceBox.getItems()::add);
        alterationChoiceBox.setValue(NO_ALTERATION);

        // Init alterations
        updateAlterationsList();

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
        alterationChoiceBox.setOnAction(e -> {
            String raw = alterationChoiceBox.getValue().toString();
            if (!ALTERATIONS_MAP.contains1(raw)) {
                return;
            }
            var parsed = ALTERATIONS_MAP.get1(raw);
            var result = manager.append(new AddAlterationToChordClipEvent(trackId, clipId, parsed.first(), parsed.second()));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to add chord extension/alteration" + result, ButtonType.OK).showAndWait();
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

    private void updateAlterationsList() {
        final var view = clip.get().getChordBuilderView();
        alterationsList.getChildren().clear();
        for (Tuple2<Accidental, Integer> alt : view.getAlterations()) {
            var box = new HBox();
            box.setSpacing(8);

            Label label = new Label(ALTERATIONS_MAP.get2(alt));
            box.getChildren().add(new AnchorPane(label));
            AnchorPane.setTopAnchor(label, 0.0);
            AnchorPane.setBottomAnchor(label, 0.0);

            var button = new Button("X");
            button.setOnAction(e -> {
                var result = manager.append(new RemoveAlterationFromChordClipEvent(trackId, clipId, alt.first(), alt.second()));
                if (!result.isOk()) {
                    new Alert(Alert.AlertType.ERROR, "Failed to remove chord extension/alteration" + result, ButtonType.OK).showAndWait();
                }
            });
            box.getChildren().add(button);

            alterationsList.getChildren().add(box);
        }
    }
}
