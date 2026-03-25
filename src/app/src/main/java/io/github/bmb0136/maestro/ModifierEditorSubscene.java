package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.event.EventTarget;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;

import java.util.UUID;

public abstract class ModifierEditorSubscene<T extends Modifier> extends SubScene implements AutoCloseable {
    protected final TimelineManager manager;
    protected final UUID trackId, clipId, modifierId;
    private final ReadOnlyObjectWrapper<T> wrapper;
    protected final ReadOnlyObjectProperty<T> modifier;
    private final AutoCloseable callback;


    public ModifierEditorSubscene(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId) {
        // Dummy node (can't pass null here)
        // Size doesn't matter, it will be automatically resized
        super(new Pane(), 1, 1);
        this.manager = manager;
        this.trackId = trackId;
        this.clipId = clipId;
        this.modifierId = modifierId;

        wrapper = new ReadOnlyObjectWrapper<>();
        modifier = wrapper.getReadOnlyProperty();
        callback = manager.registerChangeCallback(this::changeCallback);
    }

    private void changeCallback(EventTarget target) {
        if (!target.isModifier()) {
            return;
        }
        var tTrackId = target.getTrackId();
        if (tTrackId.isEmpty() || !tTrackId.get().equals(trackId)) {
            return;
        }
        var tClipId = target.getClipId();
        if (tClipId.isEmpty() || !tClipId.get().equals(clipId)) {
            return;
        }
        var tModId = target.getModifierId();
        if (tModId.isEmpty() || !tModId.get().equals(modifierId)) {
            return;
        }
        // HACK: copying since ObjectPropertyBase won't trigger updates if the instance is the same
        //noinspection unchecked
        var modifier = (T) target.getTimeline()
                .getTrack(trackId)
                .flatMap(t -> t.getClip(clipId))
                .flatMap(c -> c.getModifiers().getModifier(modifierId))
                .orElseThrow()
                .copy(false);
        wrapper.set(modifier);
    }

    @Override
    public void close() throws Exception {
        callback.close();
    }

    @FunctionalInterface
    public interface Factory {
        ModifierEditorSubscene<?> create(TimelineManager manager, UUID trackId, UUID clipId, UUID modifierId);
    }
}
