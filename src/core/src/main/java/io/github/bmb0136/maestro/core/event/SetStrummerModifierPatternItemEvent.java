package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.modifier.StrummerModifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetStrummerModifierPatternItemEvent extends ModifierEvent {
    private final int index;
    private final boolean value;

    public SetStrummerModifierPatternItemEvent(UUID trackId, UUID clipId, UUID modifierId, int index, boolean value) {
        super(trackId, clipId, modifierId);
        this.index = index;
        this.value = value;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof StrummerModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        return target.setPatternItem(index, value) ? EventResult.OK : EventResult.NOOP;
    }
}
