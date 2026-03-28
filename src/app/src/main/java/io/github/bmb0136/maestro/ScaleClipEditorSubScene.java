package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.ScaleClip;
import io.github.bmb0136.maestro.core.event.*;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.theory.ScaleType;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.util.BiHashMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.UUID;

public class ScaleClipEditorSubScene extends ClipEditorSubScene<ScaleClip> {

    private static final BiHashMap<String, ScaleType> SCALE_MAP = new BiHashMap<>();
    private static final BiHashMap<String, PitchName> PITCH_MAP = new BiHashMap<>();
    private static final BiHashMap<String, ScaleClip.Mode> MODE_MAP = new BiHashMap<>();

    static {
        for (PitchName name : PitchName.values()) {
            PITCH_MAP.add(name.toString(), name);
        }

        MODE_MAP.add("Hold", ScaleClip.Mode.HOLD);
        MODE_MAP.add("Ascending", ScaleClip.Mode.ASCENDING);
        MODE_MAP.add("Descending", ScaleClip.Mode.DESCENDING);

        for (ScaleType type : ScaleType.values()) {
            char[] temp = type.name().toCharArray();
            for (int i = 0; i < temp.length; i++) {
                var c = temp[i];
                if (c == '_') {
                    temp[i] = ' ';
                } else if (i == 0 || temp[i - 1] == ' ') {
                    temp[i] = Character.toUpperCase(c);
                } else {
                    temp[i] = Character.toLowerCase(c);
                }
            }
            SCALE_MAP.add(new String(temp), type);
        }
    }

    @FXML
    private Region root;
    @FXML
    private Node nonHoldModeOptions;
    @FXML
    private ChoiceBox<Object> scaleChoiceBox, rootPitchChoiceBox, modeChoiceBox;
    @FXML
    private TextField noteDurationTextBox;
    @FXML
    private CheckBox repeatCheckBox;
    @FXML
    private Spinner<Object> minDegreeSpinner, maxDegreeSpinner, rootOctaveSpinner;

    public ScaleClipEditorSubScene(TimelineManager manager, UUID trackId, UUID clipId) {
        super(manager, trackId, clipId);
    }

    public static ScaleClipEditorSubScene create(TimelineManager manager, UUID trackId, UUID clipId) {
        var resource = Objects.requireNonNull(App.class.getResource("/ScaleClip.fxml"));
        var loader = new FXMLLoader(resource);
        try {
            var s = new ScaleClipEditorSubScene(manager, trackId, clipId);
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

        nonHoldModeOptions.visibleProperty().bind(clip.map(c -> c.getMode() != ScaleClip.Mode.HOLD));

        // Init choice boxes
        for (ScaleType type : ScaleType.values()) {
            scaleChoiceBox.getItems().add(SCALE_MAP.get2(type));
        }
        for (PitchName name : PitchName.values()) {
            rootPitchChoiceBox.getItems().add(PITCH_MAP.get2(name));
        }
        for (ScaleClip.Mode mode : ScaleClip.Mode.values()) {
            modeChoiceBox.getItems().add(MODE_MAP.get2(mode));
        }
        scaleChoiceBox.setValue(SCALE_MAP.get2(clip.get().getScale()));
        rootPitchChoiceBox.setValue(PITCH_MAP.get2(clip.get().getRootPitch()));
        modeChoiceBox.setValue(MODE_MAP.get2(clip.get().getMode()));

        // Init spinners
        rootOctaveSpinner.setValueFactory(SpinnerUtil.toObjectValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Pitch.fromMidi(0, false).octave(),
                Pitch.fromMidi(127, false).octave(),
                clip.get().getRootOctave()
        )));
        minDegreeSpinner.setValueFactory(SpinnerUtil.toObjectValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-999, 999, clip.get().getMinDegree())));
        maxDegreeSpinner.setValueFactory(SpinnerUtil.toObjectValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-999, 999, clip.get().getMaxDegree())));

        // Init fields
        rootOctaveSpinner.getValueFactory().setValue(clip.get().getRootOctave());
        repeatCheckBox.setSelected(clip.get().isRepeat());
        noteDurationTextBox.setText(Float.toString(clip.get().getNoteDuration()));

        // Wait for changes
        clip.addListener(ignored -> {
            var clip = this.clip.get();
            scaleChoiceBox.setValue(SCALE_MAP.get2(clip.getScale()));
            rootPitchChoiceBox.setValue(PITCH_MAP.get2(clip.getRootPitch()));
            rootOctaveSpinner.getValueFactory().setValue(clip.getRootOctave());
            minDegreeSpinner.getValueFactory().setValue(clip.getMinDegree());
            maxDegreeSpinner.getValueFactory().setValue(clip.getMaxDegree());
            modeChoiceBox.setValue(MODE_MAP.get2(clip.getMode()));
            repeatCheckBox.setSelected(clip.isRepeat());
            noteDurationTextBox.setText(Float.toString(clip.getNoteDuration()));
        });

        scaleChoiceBox.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            var result = manager.append(new SetScaleClipScaleEvent(trackId, clipId, SCALE_MAP.get1(newValue.toString())));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set scale: " + result, ButtonType.OK).showAndWait();
            }
        });
        rootPitchChoiceBox.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            var result = manager.append(new SetScaleClipRootPitchEvent(trackId, clipId, PITCH_MAP.get1(newValue.toString())));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set root pitch: " + result, ButtonType.OK).showAndWait();
            }
        });
        rootOctaveSpinner.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            if (!(newValue instanceof Integer octave)) {
                throw new IllegalStateException("Root octave was not integer");
            }
            var result = manager.append(new SetScaleClipRootOctaveEvent(trackId, clipId, octave));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set root octave: " + result, ButtonType.OK).showAndWait();
            }
        });
        minDegreeSpinner.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            if (!(newValue instanceof Integer octave)) {
                throw new IllegalStateException("Min degree was not integer");
            }
            var result = manager.append(new SetScaleClipMinDegreeEvent(trackId, clipId, octave));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set min degree: " + result, ButtonType.OK).showAndWait();
            }
        });
        maxDegreeSpinner.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            if (!(newValue instanceof Integer octave)) {
                throw new IllegalStateException("Max degree was not integer");
            }
            var result = manager.append(new SetScaleClipMaxDegreeEvent(trackId, clipId, octave));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set max degree: " + result, ButtonType.OK).showAndWait();
            }
        });
        modeChoiceBox.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            var result = manager.append(new SetScaleClipModeEvent(trackId, clipId, MODE_MAP.get1(newValue.toString())));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set mode: " + result, ButtonType.OK).showAndWait();
            }
        });
        repeatCheckBox.selectedProperty().addListener((ignored, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) {
                return;
            }
            var result = manager.append(new SetScaleClipRepeatEvent(trackId, clipId, newValue));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set repeat: " + result, ButtonType.OK).showAndWait();
            }
        });
    }

    @FXML
    private void onMakeEvenButtonClicked() {
        var clip = this.clip.get();
        noteDurationTextBox.setText(String.valueOf(clip.getDuration() / (clip.getMaxDegree() - clip.getMinDegree() + 1)));
        onNoteDurationEdited();
    }

    @FXML
    private void onNoteDurationEdited() {
        try {
            float value = Float.parseFloat(noteDurationTextBox.getText().trim());
            var result = manager.append(new SetScaleClipNoteDurationEvent(trackId, clipId, value));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set note duration: " + result, ButtonType.OK).showAndWait();
            }
        } catch (NumberFormatException ignored) {
            noteDurationTextBox.setText(String.valueOf(clip.get().getNoteDuration()));
        }
    }
}
