package io.github.bmb0136.maestro.editors.modifier;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.util.BiHashMap;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class SimpleModifierEditor<T extends Modifier> extends ModifierEditorSubscene<T> {
    private static final ObservableValue<Boolean> TRUE = new SimpleBooleanProperty(true);

    private final VBox components;

    public SimpleModifierEditor(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        super(manager, trackId, clipId, modifierId);

        components = new VBox();
        components.setSpacing(8);
        components.getStylesheets().add("/DarkMode.css");
        setRoot(components);
        heightProperty().bind(components.prefHeightProperty());
    }

    protected void addInteger(String text, int min, int max, ObservableValue<Integer> getter, Consumer<Integer> valueChanged) {
        addInteger(text, min, max, getter, valueChanged, TRUE);
    }

    protected void addInteger(String text, int min, int max, ObservableValue<Integer> getter, Consumer<Integer> valueChanged, ObservableValue<Boolean> visibleCondition) {
        var spinner = new Spinner<Integer>();
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, getter.getValue());
        spinner.setValueFactory(factory);
        getter.addListener((ignored1, ignored2, newValue) ->
                factory.setValue(newValue));
        spinner.valueProperty().addListener((ignored, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                valueChanged.accept(newValue);
            }
        });
        addLabeled(text, spinner, visibleCondition);
    }

    protected void addFloat(String text, float min, float max, ObservableValue<Float> getter, Consumer<Float> valueChanged) {
        addFloat(text, min, max, getter, valueChanged, TRUE);
    }

    protected void addFloat(String text, float min, float max, ObservableValue<Float> getter, Consumer<Float> valueChanged, ObservableValue<Boolean> visibleCondition) {
        var field = new TextField();
        getter.addListener((ignored1, ignored2, newValue) ->
                field.setText(newValue.toString()));
        field.setOnAction(e -> {
            try {
                var value = Float.parseFloat(field.getText());
                if (value >= min && value <= max) {
                    valueChanged.accept(value);
                } else {
                    field.setText(getter.getValue().toString());
                }
            } catch (NumberFormatException ignored) {
                field.setText(getter.getValue().toString());
            }
        });
        addLabeled(text, field, visibleCondition);
    }

    protected <E extends Enum<E>> void addEnum(String text, BiHashMap<String, E> converter, List<String> order, ObservableValue<E> getter, Consumer<E> valueChanged) {
        addEnum(text, converter, order, getter, valueChanged, TRUE);
    }

    protected <E extends Enum<E>> void addEnum(String text, BiHashMap<String, E> converter, List<String> order, ObservableValue<E> getter, Consumer<E> valueChanged, ObservableValue<Boolean> visibleCondition) {
        var box = new ChoiceBox<String>();
        order.forEach(box.getItems()::add);
        getter.addListener((ignored1, ignored2, newValue) ->
                box.setValue(converter.get2(newValue)));
        box.valueProperty().addListener((ignored1, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && converter.contains1(newValue)) {
                valueChanged.accept(converter.get1(newValue));
            }
        });
        addLabeled(text, box, visibleCondition);
    }

    protected void addBoolean(String text, ObservableValue<Boolean> getter, Consumer<Boolean> valueChanged) {
        addBoolean(text, getter, valueChanged, TRUE);
    }

    protected void addBoolean(String text, ObservableValue<Boolean> getter, Consumer<Boolean> valueChanged, ObservableValue<Boolean> visibleCondition) {
        var check = new CheckBox();
        getter.addListener((ignored1, ignored2, newValue) ->
                check.setSelected(newValue));
        check.selectedProperty().addListener((ignored1, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                valueChanged.accept(newValue);
            }
        });
        addLabeled(text, check, visibleCondition);
    }

    protected void addButton(String text, Runnable onClick) {
        addButton(text, onClick, TRUE);
    }

    protected void addButton(String text, Runnable onClick, ObservableValue<Boolean> visibleCondition) {
        var button = new Button(text);
        button.setOnAction(e -> onClick.run());
        button.maxWidthProperty().bind(components.widthProperty());

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(0, 8, 0, 8));
        hbox.getChildren().add(button);
        HBox.setHgrow(button, Priority.ALWAYS);

        components.getChildren().add(hbox);
        hbox.heightProperty().addListener(this::recalculateHeight);
        hbox.visibleProperty().bind(visibleCondition);
    }

    protected void addLabeled(String text, Node node, ObservableValue<Boolean> visibleCondition) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(0, 8, 0, 8));
        var children = hbox.getChildren();

        var label = new Label(text);
        children.add(new AnchorPane(label));
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        HBox.setHgrow(label.getParent(), Priority.ALWAYS);

        children.add(node);

        hbox.prefWidthProperty().bind(components.widthProperty());
        components.getChildren().add(hbox);

        hbox.heightProperty().addListener(this::recalculateHeight);
        hbox.visibleProperty().bind(visibleCondition);
    }

    private void recalculateHeight(Observable ignored) {
        double total = 0;
        for (Node node : components.getChildren()) {
            if ((node instanceof Region r)) {
                total += r.getHeight();
            }
        }
        components.setPrefHeight(total);
    }
}
