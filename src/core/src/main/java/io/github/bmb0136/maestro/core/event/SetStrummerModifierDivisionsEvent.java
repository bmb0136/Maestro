package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.modifier.StrummerModifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetStrummerModifierDivisionsEvent extends ModifierEvent {
    private final int newDivisions;

    public SetStrummerModifierDivisionsEvent(UUID trackId, UUID clipId, UUID modifierId, int newDivisions) {
        super(trackId, clipId, modifierId);
        this.newDivisions = newDivisions;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof StrummerModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        var oldDivisions = target.getDivisions();
        target.setDivisions(newDivisions);
        return newDivisions == oldDivisions ? EventResult.NOOP : EventResult.OK;
    }
}

