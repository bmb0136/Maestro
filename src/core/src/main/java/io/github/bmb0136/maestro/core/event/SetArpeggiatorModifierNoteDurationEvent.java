package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.ArpeggiatorModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetArpeggiatorModifierNoteDurationEvent extends ModifierEvent {
    private final float newNoteDuration;

    public SetArpeggiatorModifierNoteDurationEvent(UUID trackId, UUID clipId, UUID modifierId, float newNoteDuration) {
        super(trackId, clipId, modifierId);
        this.newNoteDuration = newNoteDuration;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof ArpeggiatorModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        var oldNoteDuration = target.getNoteDuration();
        target.setNoteDuration(newNoteDuration);
        return oldNoteDuration == newNoteDuration ? EventResult.NOOP : EventResult.OK;
    }
}
