package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.modifier.OffsetByIntervalModifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetOffsetByIntervalModifierSemitonesEvent extends ModifierEvent {
    private final int newSemitones;

    public SetOffsetByIntervalModifierSemitonesEvent(UUID trackId, UUID clipId, UUID modifierId, int newSemitones) {
        super(trackId, clipId, modifierId);
        this.newSemitones = newSemitones;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof OffsetByIntervalModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        int oldSemitones = target.getSemitones();
        target.setSemitones(newSemitones);
        return oldSemitones == newSemitones ? EventResult.NOOP : EventResult.OK;
    }
}
