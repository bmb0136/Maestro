package io.github.bmb0136.maestro.playback;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.event.EventTarget;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaybackEngine implements AutoCloseable {
    protected final AtomicInteger bpm = new AtomicInteger();
    protected final HashMap<UUID, PlaybackActionQueue> clipQueues = new HashMap<>();
    protected final ReadOnlyBooleanWrapper isPlaying = new ReadOnlyBooleanWrapper();
    protected final Semaphore stoppedSemaphore = new Semaphore(0);
    private final PlaybackThread thread;
    private final AutoCloseable changeCallback;
    private final ReadOnlyIntegerWrapper bpmWrapper = new ReadOnlyIntegerWrapper();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private final ArrayList<ScheduledFuture<?>> scheduled = new ArrayList<>();
    // Used to detect track/clip changes
    private final HashSet<UUID> knownTracks = new HashSet<>();
    private final HashMap<UUID, Set<UUID>> clipsByTrack = new HashMap<>();
    // Used by states for synthesis
    protected Synthesizer synthesizer;
    protected MidiChannel[] channels;
    // Used to calculate current position
    protected float lastPosition;
    protected long lastActionTime;
    protected int lastBpm;

    public PlaybackEngine(TimelineManager manager) {
        thread = new PlaybackThread(this);
        changeCallback = manager.registerChangeCallback(this::onTimelineChanged);

        initSynth(null);
        setBpm(120);

        thread.start();
    }

    public int getBpm() {
        return bpm.get();
    }

    public void setBpm(int bpm) {
        bpm = Math.max(1, bpm);
        if (canSetBpm()) {
            this.bpm.set(bpm);
            bpmWrapper.set(bpm);
        }
    }

    public ReadOnlyIntegerProperty bpmProperty() {
        return bpmWrapper.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty isPlayingProperty() {
        return isPlaying.getReadOnlyProperty();
    }

    public float getPositionInBeats() {
        if (!isPlaying.get()) {
            return lastPosition;
        }

        return calculateLastPosition();
    }

    private float calculateLastPosition() {
        float offset = (System.currentTimeMillis() - lastActionTime) / 60_000.0f * lastBpm;
        return lastPosition + offset;
    }

    public boolean canSetBpm() {
        return !isPlaying.get();
    }

    public void initSynth(@Nullable Synthesizer synth) {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }

        if (synth == null) {
            try {
                synthesizer = MidiSystem.getSynthesizer();
                synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
            } catch (MidiUnavailableException e) {
                throw new RuntimeException(e);
            }
        } else {
            synthesizer = synth;
        }
        if (!synthesizer.isOpen()) {
            try {
                synthesizer.open();
            } catch (MidiUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
        channels = synthesizer.getChannels();
    }

    public void start() {
        start(0);
    }

    public void start(float position) {
        if (isPlaying.get()) {
            seek(position);
            return;
        }

        lastPosition = position;
        lastActionTime = System.currentTimeMillis();
        lastBpm = getBpm();
        sendMessage(new PlaybackMessage.Start(position));
    }

    public void seek(float position) {
        scheduled.forEach(f -> f.cancel(true));
        synchronized (clipQueues) {
            clipQueues.values().forEach(q -> q.seek(position));
        }

        lastPosition = position;
        lastActionTime = System.currentTimeMillis();
        lastBpm = getBpm();

        sendMessage(new PlaybackMessage.Seek(position));
    }

    public void stop() {
        if (!isPlaying.get()) {
            return;
        }

        sendMessage(new PlaybackMessage.Stop());
        try {
            stoppedSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lastBpm = getBpm();
        lastPosition = calculateLastPosition();
    }

    public void scheduleMessage(@NotNull PlaybackMessage message, long delay, TimeUnit delayUnit) {
        scheduled.add(executor.schedule(() -> sendMessage(message), delay, delayUnit));
    }

    protected void sendMessage(@NotNull PlaybackMessage message) {
        thread.messages.add(message);
        thread.messageSemaphore.release();
    }

    private void onTimelineChanged(EventTarget target) {
        var timeline = target.getTimeline();

        // Check for track add/remove
        if (target.isTimeline()) {
            // Note: build this set "backwards," i.e., copy knownTracks and remove all that exist.
            // Then, any tracks remaining must have deleted
            HashSet<UUID> deleted = new HashSet<>(knownTracks);
            knownTracks.clear();

            for (Track track : timeline) {
                var trackId = track.getId();
                knownTracks.add(trackId);

                // If the track did not exist before
                if (!deleted.remove(trackId)) {
                    onTrackUpdated(track);
                }
            }

            synchronized (clipQueues) {
                for (UUID trackId : deleted) {
                    clipsByTrack.remove(trackId).forEach(clipQueues::remove);
                }
            }
        }

        // Check for clip add/remove
        if (target.isTrack()) {
            onTrackUpdated(target.getTrackId().flatMap(timeline::getTrack).orElseThrow());
        }

        // Check for modifier add/remove/edit
        if (target.isClip() || target.isModifier()) {
            var track = target.getTrackId().flatMap(timeline::getTrack).orElseThrow();
            var clip = target.getClipId().flatMap(track::getClip).orElseThrow();
            onClipUpdated(clip);
        }
    }

    private void onTrackUpdated(Track track) {
        // Same logic as with tracks
        var knownClips = clipsByTrack.computeIfAbsent(track.getId(), ignored -> new HashSet<>());
        HashSet<UUID> deleted = new HashSet<>(knownClips);
        knownClips.clear();

        for (Clip clip : track) {
            var id = clip.getId();
            knownClips.add(id);

            // Same logic as with tracks
            if (!deleted.remove(id)) {
                onClipUpdated(clip);
            }
        }

        synchronized (clipQueues) {
            for (UUID clipId : deleted) {
                clipQueues.remove(clipId);
            }
        }
    }

    private void onClipUpdated(Clip clip) {
        synchronized (clipQueues) {
            var queue = clipQueues.computeIfAbsent(clip.getId(), ignored -> new PlaybackActionQueue());
            queue.clear();
            queue.addFromClip(clip);
        }
    }

    @Override
    public void close() throws Exception {
        executor.close();
        changeCallback.close();
        thread.close();
    }
}
