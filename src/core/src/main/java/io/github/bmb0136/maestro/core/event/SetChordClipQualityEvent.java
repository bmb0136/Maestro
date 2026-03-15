package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.ChordQuality;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetChordClipQualityEvent extends ClipEvent {
    private final ChordQuality newQuality;

    public SetChordClipQualityEvent(UUID trackId, UUID clipId, ChordQuality newQuality) {
        super(trackId, clipId);
        this.newQuality = newQuality;
    }

    @Override
    public EventResult apply(@NotNull EventContext<Clip> context) {
        if (!(context.target() instanceof ChordClip target)) {
            return EventResult.WRONG_CLIP_TYPE;
        }
        var oldQuality = target.getChordBuilderView().getQuality();
        target.setQuality(newQuality);
        return oldQuality == newQuality ? EventResult.NOOP : EventResult.OK;
    }
}
