package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;

import java.util.UUID;

public abstract class ClipEditorSubScene<T extends Clip> extends SubScene {

    protected final SimpleObjectProperty<T> clip;
    protected final TimelineManager manager;
    protected final UUID trackId, clipId;

    public ClipEditorSubScene(TimelineManager manager, UUID trackId, UUID clipId) {
        // Dummy node (can't pass null here)
        // Size doesn't matter, it will be automatically resized
        super(new Pane(), 1, 1);
        this.manager = manager;
        this.trackId = trackId;
        this.clipId = clipId;
        //noinspection unchecked
        clip = new SimpleObjectProperty<>((T) manager.get().getTrack(trackId).flatMap(t -> t.getClip(clipId)).orElseThrow());
    }
}
