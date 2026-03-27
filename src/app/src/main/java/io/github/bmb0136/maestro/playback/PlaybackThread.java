package io.github.bmb0136.maestro.playback;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlaybackThread extends Thread implements AutoCloseable {
    protected final AtomicReference<PlaybackState.Type> currentState = new AtomicReference<>();
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
            currentState.set(state.getType());
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
