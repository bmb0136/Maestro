package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.AddIntervalAboveModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetAddIntervalAboveModifierSemitonesEvent extends ModifierEvent {
    private final int newSemitones;

    protected SetAddIntervalAboveModifierSemitonesEvent(UUID trackId, UUID clipId, UUID modifierId, int newSemitones) {
        super(trackId, clipId, modifierId);
        this.newSemitones = newSemitones;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Modifier> context) {
        if (!(context.target() instanceof AddIntervalAboveModifier target)) {
            return EventResult.WRONG_MODIFIER_TYPE;
        }
        int oldSemitones = target.getSemitones();
        target.setSemitones(newSemitones);
        return oldSemitones == newSemitones ? EventResult.NOOP : EventResult.OK;
    }
}
