package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.Accidental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RemoveAlterationFromChordClipEvent extends ClipEvent {
    private final Accidental accidental;
    private final int scaleDegree;

    public RemoveAlterationFromChordClipEvent(UUID trackId, UUID clipId, @Nullable Accidental accidental, int scaleDegree) {
        super(trackId, clipId);
        this.accidental = accidental;
        this.scaleDegree = scaleDegree;
    }

    public RemoveAlterationFromChordClipEvent(UUID trackId, UUID clipId, int scaleDegree) {
        this(trackId, clipId, null, scaleDegree);
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldAlteration = target.getChordBuilderView().getAlteration(scaleDegree);
        if (oldAlteration.isEmpty()) {
            return EventResult.NOOP;
        }
        if (accidental != null) {
            target.removeAlteration(accidental, scaleDegree);
        } else {
            target.removeAlteration(scaleDegree);
        }
        return EventResult.OK;
    }
}
