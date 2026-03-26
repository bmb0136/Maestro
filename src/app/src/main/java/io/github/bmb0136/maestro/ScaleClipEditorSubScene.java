package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.ScaleClip;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.UUID;

public class ScaleClipEditorSubScene extends ClipEditorSubScene<ScaleClip> {
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
    private Spinner<Object> minDegreeSpinner, maxDegreeSpinner, rootOcatveSpinner;

    public ScaleClipEditorSubScene(TimelineManager manager, UUID trackId, UUID clipId) {
        super(manager, trackId, clipId);
    }

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        nonHoldModeOptions.visibleProperty().bind(clip.map(c -> c.getMode() != ScaleClip.Mode.HOLD));
    }

    @FXML
    private void onMakeEvenButtonClicked() {
        System.out.println("TODO");
    }

    @FXML
    private void onNoteDurationEdited() {
        System.out.println("TODO");
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
}
