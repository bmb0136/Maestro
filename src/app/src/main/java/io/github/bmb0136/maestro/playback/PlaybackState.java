package io.github.bmb0136.maestro.playback;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class PlaybackState implements AutoCloseable {
    protected final PlaybackEngine engine;
    private final Type type;

    private PlaybackState(@NotNull Type type, @NotNull PlaybackEngine engine) {
        this.type = type;
        this.engine = engine;
    }

    public Type getType() {
        return type;
    }

    public void init() {
    }

    public abstract PlaybackState handle(@NotNull PlaybackMessage message);

    @Override
    public void close() {
    }

    public enum Type {
        IDLE,
        PLAYING
    }

    public static final class IdleState extends PlaybackState {
        public IdleState(@NotNull PlaybackEngine engine) {
            super(Type.IDLE, engine);
        }

        @Override
        public PlaybackState handle(@NotNull PlaybackMessage message) {
            return switch (message) {
                case PlaybackMessage.Start m -> new PlayState(engine, System.currentTimeMillis(), m.getPosition());
                default -> this;
            };
        }
    }

    public static final class PlayState extends PlaybackState {
        private final long startTimeMillis;
        private final float startTimeBeats;

        public PlayState(@NotNull PlaybackEngine engine, long startTimeMillis, float startTimeBeats) {
            super(Type.PLAYING, engine);
            this.startTimeMillis = startTimeMillis;
            this.startTimeBeats = startTimeBeats;
        }

        @Override
        public void init() {
            // Determine which action to perform
            UUID clipId = null;
            PlaybackActionQueue.Action action = null;
            synchronized (engine.clipQueues) {
                for (var entry : engine.clipQueues.entrySet()) {
                    var head = entry.getValue().getHead();
                    if (head == null) {
                        continue;
                    }

                    if (action == null || head.timeBeats() < action.timeBeats()) {
                        clipId = entry.getKey();
                        action = head;
                    }
                }
            }

            if (action == null) {
                return;
            }

            // Consume action
            var q = engine.clipQueues.get(clipId);
            q.setOffset(q.getOffset() + 1);

            var bpm = engine.getBpm();
            var now = System.currentTimeMillis();

            // Track last action
            engine.lastBpm = bpm;
            engine.lastActionTime = now;
            engine.lastPosition = action.timeBeats();

            // Determine how long to wait
            long millis = (long) (60_000.0f / bpm * (action.timeBeats() - startTimeBeats));
            long delay = startTimeMillis + millis - now;

            var message = new PlaybackMessage.PerformAction(action);
            if (delay >= 5) {
                engine.scheduleMessage(message, delay, TimeUnit.MILLISECONDS);
            } else {
                engine.sendMessage(message);
            }
        }

        @Override
        public PlaybackState handle(@NotNull PlaybackMessage message) {
            return switch (message) {
                case PlaybackMessage.Seek m -> new PlayState(engine, System.currentTimeMillis(), m.getPosition());
                case PlaybackMessage.PerformAction m -> {
                    var action = m.getAction();
                    if (action.on()) {
                        engine.channels[0].noteOn(action.note(), action.velocity());
                    } else {
                        engine.channels[0].noteOff(action.note());
                    }
                    yield this;
                }
                case PlaybackMessage.Stop ignored -> new IdleState(engine);
                default -> this;
            };
        }

        @Override
        public void close() {
            for (var c : engine.channels) {
                c.allNotesOff();
            }
        }
    }
}
