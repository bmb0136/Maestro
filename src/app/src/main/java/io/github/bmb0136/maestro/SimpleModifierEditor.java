package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class SimpleModifierEditor<T extends Modifier> extends ModifierEditorSubscene<T> {
    private final VBox components;

    public SimpleModifierEditor(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        super(manager, trackId, clipId, modifierId);

        components = new VBox();
        components.setSpacing(8);
        components.getStylesheets().add("/DarkMode.css");
        setRoot(components);
        // TODO: fix height
    }

    protected void addInteger(String text, int min, int max, ObservableValue<Integer> getter, Consumer<Integer> valueChanged) {
        var spinner = new Spinner<Integer>();
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max);
        spinner.setValueFactory(factory);
        getter.addListener((ignored1, ignored2, newValue) ->
                factory.setValue(newValue));
        spinner.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                valueChanged.accept(newValue);
            }
        });
        addLabeled(text, spinner);
    }

    protected void addLabeled(String text, Node node) {
        HBox hbox = new HBox();
        var children = hbox.getChildren();

        var label = new Label(text);
        children.add(new AnchorPane(label));
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        HBox.setHgrow(label.getParent(), Priority.ALWAYS);

        children.add(node);

        hbox.prefWidthProperty().bind(components.widthProperty());
        components.getChildren().add(hbox);
    }
}
