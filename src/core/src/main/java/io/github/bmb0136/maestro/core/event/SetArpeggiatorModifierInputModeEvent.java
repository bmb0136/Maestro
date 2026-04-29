package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.ArpeggiatorModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetArpeggiatorModifierInputModeEvent extends ModifierEvent {
    private final ArpeggiatorModifier.InputMode newMode;

    public SetArpeggiatorModifierInputModeEvent(UUID trackId, UUID clipId, UUID modifierId, ArpeggiatorModifier.InputMode newMode) {
        super(trackId, clipId, modifierId);
        this.newMode = newMode;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof ArpeggiatorModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        var oldMode = target.getInputMode();
        target.setInputMode(newMode);
        return oldMode == newMode ? EventResult.NOOP : EventResult.OK;
    }
}
