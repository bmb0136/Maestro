package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.modifier.ModifierList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MoveModifierPreviousEvent extends ClipEvent {
    @Nullable
    private final UUID modifierId;
    private final int modifierIndex;

    public MoveModifierPreviousEvent(UUID trackId, UUID clipId, @NotNull UUID modifierId) {
        super(trackId, clipId);
        this.modifierId = modifierId;
        modifierIndex = -1;
    }

    public MoveModifierPreviousEvent(UUID trackId, UUID clipId, int modifierIndex) {
        super(trackId, clipId);
        modifierId = null;
        this.modifierIndex = modifierIndex;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        ModifierList modifiers = context.target().getModifiers();
        int index = modifierIndex;
        if (modifierId != null) {
            index = modifiers.indexOf(modifierId).orElse(-1);
        }
        if (index < 0 || index >= modifiers.size()) {
            return EventResult.UNKNOWN_MODIFIER;
        }
        return modifiers.movePrevious(index) ? EventResult.OK : EventResult.NOOP;
    }
}
