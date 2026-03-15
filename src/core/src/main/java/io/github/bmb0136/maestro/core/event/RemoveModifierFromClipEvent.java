package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoveModifierFromClipEvent extends ClipEvent {
    private final UUID modifierId;

    protected RemoveModifierFromClipEvent(UUID trackId, UUID clipId, UUID modifierId) {
        super(trackId, clipId);
        this.modifierId = modifierId;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        return context.target().getModifiers().removeModifier(modifierId) ? EventResult.OK : EventResult.NOOP;
    }
}
