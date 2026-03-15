package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AddModifierToClipEvent extends ClipEvent {
    private final Modifier modifier;

    protected AddModifierToClipEvent(UUID trackId, UUID clipId, @NotNull Modifier modifier) {
        super(trackId, clipId);
        this.modifier = modifier;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        Clip target = context.target();
        if (target.getModifiers().getModifier(modifier.getId()).isPresent()) {
            return EventResult.MODIFIER_ALREADY_ON_CLIP;
        }
        // TODO: Should we allow "linking" modifiers between clips, or should be error if the same modifier is added to multiple clips?
        target.getModifiers().addModifier(modifier);
        return EventResult.OK;
    }
}
