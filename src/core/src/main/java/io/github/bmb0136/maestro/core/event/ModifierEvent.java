package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.modifier.Modifier;

import java.util.UUID;

public abstract class ModifierEvent extends Event<Modifier> {
    private final UUID trackId, clipId, modifierId;

    protected ModifierEvent(UUID trackId, UUID clipId, UUID modifierId) {
        this.trackId = trackId;
        this.clipId = clipId;
        this.modifierId = modifierId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public UUID getClipId() {
        return clipId;
    }

    public UUID getModifierId() {
        return modifierId;
    }
}
