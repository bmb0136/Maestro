package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.ArpeggiatorModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetArpeggiatorModifierRepeatEvent extends ModifierEvent {
    private final boolean newRepeat;

    public SetArpeggiatorModifierRepeatEvent(UUID trackId, UUID clipId, UUID modifierId, boolean newRepeat) {
        super(trackId, clipId, modifierId);
        this.newRepeat = newRepeat;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof ArpeggiatorModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        var oldRepeat = target.isRepeat();
        target.setRepeat(newRepeat);
        return oldRepeat == newRepeat ? EventResult.NOOP : EventResult.OK;
    }
}
