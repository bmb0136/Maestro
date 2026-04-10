package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.Accidental;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AddAlterationToChordClipEvent extends ClipEvent {
    private final Accidental accidental;
    private final int scaleDegree;

    public AddAlterationToChordClipEvent(UUID trackId, UUID clipId, @NotNull Accidental accidental, int scaleDegree) {
        super(trackId, clipId);
        this.accidental = accidental;
        this.scaleDegree = scaleDegree;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldAlteration = target.getChordBuilderView().getAlteration(scaleDegree);
        if (oldAlteration.map(a -> a == accidental).orElse(false)) {
            return EventResult.NOOP;
        }
        target.addAlteration(accidental, scaleDegree);
        return EventResult.OK;
    }
}
