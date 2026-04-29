package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.ArpeggiatorModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetArpeggiatorModifierOutputModeEvent extends ModifierEvent {
    private final ArpeggiatorModifier.OutputMode newMode;

    public SetArpeggiatorModifierOutputModeEvent(UUID trackId, UUID clipId, UUID modifierId, ArpeggiatorModifier.OutputMode newMode) {
        super(trackId, clipId, modifierId);
        this.newMode = newMode;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof ArpeggiatorModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        var oldMode = target.getOutputMode();
        target.setOutputMode(newMode);
        return oldMode == newMode ? EventResult.NOOP : EventResult.OK;
    }
}
