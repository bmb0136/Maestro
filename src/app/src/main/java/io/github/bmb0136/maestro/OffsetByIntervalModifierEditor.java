package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.SetOffsetByIntervalModifierSemitonesEvent;
import io.github.bmb0136.maestro.core.modifier.OffsetByIntervalModifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.UUID;

public class OffsetByIntervalModifierEditor extends SimpleModifierEditor<OffsetByIntervalModifier> {
    public OffsetByIntervalModifierEditor(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        super(manager, trackId, clipId, modifierId);

        addInteger("Semitones", -999, 999, modifier.map(OffsetByIntervalModifier::getSemitones), val -> {
            var result = manager.append(new SetOffsetByIntervalModifierSemitonesEvent(trackId, clipId, modifierId, val));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to set Offset by Interval modifier semitones: " + result, ButtonType.OK).showAndWait();
            }
        });
    }
}
