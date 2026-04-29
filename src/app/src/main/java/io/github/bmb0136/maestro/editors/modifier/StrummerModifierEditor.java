package io.github.bmb0136.maestro.editors.modifier;

import io.github.bmb0136.maestro.core.event.SetStrummerModifierDivisionsEvent;
import io.github.bmb0136.maestro.core.event.SetStrummerModifierPatternItemEvent;
import io.github.bmb0136.maestro.core.modifier.StrummerModifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.util.Tuple2;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class StrummerModifierEditor extends SimpleModifierEditor<StrummerModifier> {
    private static final double PIXELS_PER_DIVISION = 32;
    private static final double DIVISION_PAD = 2;
    private static final double SIDE_PAD = 8;
    private final AnchorPane patternPane;

    public StrummerModifierEditor(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        super(manager, trackId, clipId, modifierId);

        addInteger("Divisions", 1, 1337, modifier.map(StrummerModifier::getDivisions), divs -> {
            var result = manager.append(new SetStrummerModifierDivisionsEvent(trackId, clipId, modifierId, divs));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set strummer modifier divisions: " + result, ButtonType.OK).showAndWait();
            }
        });

        patternPane = new AnchorPane();
        patternPane.minHeightProperty().bind(patternPane.prefHeightProperty());
        patternPane.setMaxHeight(Double.MAX_VALUE);

        modifier.addListener((ignored1, ignored2, newValue) ->
                updatePatternPane(newValue.getDivisions(), newValue.renderPatternPositions()));
        var initValue = modifier.get();

        patternPane.setOnMouseClicked(this::onPatternClicked);

        addCustom(patternPane);
        updatePatternPane(initValue.getDivisions(), initValue.renderPatternPositions());
    }

    private void onPatternClicked(MouseEvent e) {
        boolean value;
        switch (e.getButton()) {
            case PRIMARY -> value = true;
            case SECONDARY -> value = false;
            default -> {
                return;
            }
        }

        int index = (int) (e.getY() / PIXELS_PER_DIVISION);
        var result = manager.append(new SetStrummerModifierPatternItemEvent(trackId, clipId, modifierId, index, value));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Failed to set strummer modifier pattern item: " + result, ButtonType.OK).showAndWait();
        }
    }

    private void updatePatternPane(int divisions, @NotNull ArrayList<Tuple2<Float, Float>> positions) {
        patternPane.getChildren().clear();
        patternPane.setPrefHeight(divisions * PIXELS_PER_DIVISION);

        for (int i = 0; i < divisions; i++) {
            var line = new Line();
            line.setStartX(SIDE_PAD);
            line.setStartY(i * PIXELS_PER_DIVISION);
            line.endXProperty().bind(patternPane.widthProperty().subtract(2 * SIDE_PAD));
            line.endYProperty().bind(line.startYProperty());
            line.setStroke(Color.gray(1.0, 0.5));
            patternPane.getChildren().add(line);
        }

        for (Tuple2<Float, Float> pos : positions) {
            Rectangle rect = new Rectangle();
            rect.setFill(Color.BLUE);
            rect.setArcWidth(PIXELS_PER_DIVISION / 2);
            rect.setArcHeight(rect.getArcWidth());
            rect.setX(SIDE_PAD);
            rect.widthProperty().bind(patternPane.widthProperty().subtract(2 * SIDE_PAD));
            rect.yProperty().bind(patternPane.heightProperty().multiply(pos.first()).add(DIVISION_PAD));
            rect.heightProperty().bind(patternPane.heightProperty().multiply(pos.second()).subtract(2 * DIVISION_PAD));
            patternPane.getChildren().add(rect);
        }
    }
}
