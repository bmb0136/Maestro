package io.github.bmb0136.maestro.playback;

import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaybackThread extends Thread implements AutoCloseable {
    protected final ConcurrentLinkedQueue<PlaybackMessage> messages = new ConcurrentLinkedQueue<>();
    protected final Semaphore messageSemaphore = new Semaphore(0);
    private final PlaybackEngine engine;
    private final AtomicBoolean running = new AtomicBoolean();

    public PlaybackThread(@NotNull PlaybackEngine engine) {
        this.engine = engine;
        setName("Maestro Playback Thread");
    }

    @Override
    public void run() {
        PlaybackState state = new PlaybackState.IdleState(engine);
        running.set(true);
        while (running.get()) {
            // Update playing property (Make sure to do it on the UI thread though)
            final var isPlaying = state.getType() == PlaybackState.Type.PLAYING;
            Platform.runLater(() -> engine.isPlaying.set(isPlaying));
            if (!isPlaying && engine.stoppedSemaphore.hasQueuedThreads()) {
                engine.stoppedSemaphore.release();
            }

            // Note: `oldState` is unused but will be closed automatically
            var oldState = state;
            try {
                messageSemaphore.acquire();
                state = oldState.handle(messages.remove());
            } catch (InterruptedException ignored) {
                // Logic will be handled by oldState::close()
            } finally {
                if (state != oldState) {
                    oldState.close();
                    state.init();
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        running.set(false);
        interrupt();
        join();
    }
}
