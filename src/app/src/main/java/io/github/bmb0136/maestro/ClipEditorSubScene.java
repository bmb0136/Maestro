package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.event.EventTarget;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;

import java.util.UUID;

public abstract class ClipEditorSubScene<T extends Clip> extends SubScene implements AutoCloseable {

    protected final TimelineManager manager;
    protected final UUID trackId, clipId;
    protected final ReadOnlyObjectProperty<T> clip;
    private final AutoCloseable callback;
    private final ReadOnlyObjectWrapper<T> clipWrapper;

    public ClipEditorSubScene(TimelineManager manager, UUID trackId, UUID clipId) {
        // Dummy node (can't pass null here)
        // Size doesn't matter, it will be automatically resized
        super(new Pane(), 1, 1);
        this.manager = manager;
        this.trackId = trackId;
        this.clipId = clipId;
        //noinspection unchecked
        clipWrapper = new ReadOnlyObjectWrapper<>((T) manager.get().getTrack(trackId).flatMap(t -> t.getClip(clipId)).orElseThrow());
        clip = clipWrapper.getReadOnlyProperty();
        callback = manager.registerChangeCallback(this::changeCallback);
    }

    private void changeCallback(EventTarget target) {
        if (!target.isClip()) {
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
        // HACK: copying since ObjectPropertyBase won't trigger updates if the instance is the same
        //noinspection unchecked
        var clip = (T) target.getTimeline().getTrack(trackId).flatMap(t -> t.getClip(clipId)).orElseThrow().copy(false);
        clipWrapper.set(clip);
    }

    @Override
    public void close() throws Exception {
        callback.close();
    }
}
