package io.github.bmb0136.maestro.editors.modifier;

import io.github.bmb0136.maestro.core.event.*;
import io.github.bmb0136.maestro.core.modifier.ArpeggiatorModifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.util.BiHashMap;
import io.github.bmb0136.maestro.core.util.StringUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ArpeggiatorModifierEditor extends SimpleModifierEditor<ArpeggiatorModifier> {
    private static final BiHashMap<String, ArpeggiatorModifier.InputMode> INPUT_MAP = new BiHashMap<>();
    private static final BiHashMap<String, ArpeggiatorModifier.OutputMode> OUTPUT_MAP = new BiHashMap<>();

    static {
        for (var value : ArpeggiatorModifier.InputMode.values()) {
            INPUT_MAP.add(StringUtils.upperSnakeCaseToTitleCase(value.name()), value);
        }
        for (var value : ArpeggiatorModifier.OutputMode.values()) {
            OUTPUT_MAP.add(StringUtils.upperSnakeCaseToTitleCase(value.name()), value);
        }
    }

    public ArpeggiatorModifierEditor(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        super(manager, trackId, clipId, modifierId);

        addEnum("Input Mode", INPUT_MAP, new ArrayList<>(INPUT_MAP.values1()), modifier.map(ArpeggiatorModifier::getInputMode), mode -> {
            var result = manager.append(new SetArpeggiatorModifierInputModeEvent(trackId, clipId, modifierId, mode));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier input mode: " + result, ButtonType.OK).showAndWait();
            }
        });
        addEnum("Output Mode", OUTPUT_MAP, new ArrayList<>(OUTPUT_MAP.values1()), modifier.map(ArpeggiatorModifier::getOutputMode), mode -> {
            var result = manager.append(new SetArpeggiatorModifierOutputModeEvent(trackId, clipId, modifierId, mode));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier output mode: " + result, ButtonType.OK).showAndWait();
            }
        });
        var isRandom = modifier.map(m -> m.getOutputMode() == ArpeggiatorModifier.OutputMode.RANDOM);
        addInteger("Random Seed", Integer.MIN_VALUE, Integer.MAX_VALUE, modifier.map(ArpeggiatorModifier::getSeed), seed -> {
            var result = manager.append(new SetArpeggiatorModifierSeedEvent(trackId, clipId, modifierId, seed));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier seed: " + result, ButtonType.OK).showAndWait();
            }
        }, isRandom);
        addButton("New Seed", () -> {
            var result = manager.append(new SetArpeggiatorModifierSeedEvent(trackId, clipId, modifierId, new Random(System.currentTimeMillis()).nextInt()));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier seed: " + result, ButtonType.OK).showAndWait();
            }
        }, isRandom);
        addBoolean("Repeat", modifier.map(ArpeggiatorModifier::isRepeat), rep -> {
            var result = manager.append(new SetArpeggiatorModifierRepeatEvent(trackId, clipId, modifierId, rep));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier repeat: " + result, ButtonType.OK).showAndWait();
            }
        });
        addFloat("Note Duration", 0, 1000, modifier.map(ArpeggiatorModifier::getNoteDuration), dur -> {
            var result = manager.append(new SetArpeggiatorModifierNoteDurationEvent(trackId, clipId, modifierId, dur));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Arpeggiator modifier note duration: " + result, ButtonType.OK).showAndWait();
            }
        });
    }
}
