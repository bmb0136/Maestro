package io.github.bmb0136.maestro.playback;

public abstract class PlaybackMessage {
    private PlaybackMessage() {
    }

    public static final class Start extends PlaybackMessage {
        private final float position;

        public Start() {
            this(0);
        }

        public Start(float position) {
            this.position = position;
        }

        public float getPosition() {
            return position;
        }
    }

    public static final class Seek extends PlaybackMessage {
        private final float position;

        public Seek(float position) {
            this.position = position;
        }

        public float getPosition() {
            return position;
        }
    }

    public static final class Stop extends PlaybackMessage {
        public Stop() {
        }
    }

    public static final class PerformAction extends PlaybackMessage {
        private final PlaybackActionQueue.Action action;

        public PerformAction(PlaybackActionQueue.Action action) {
            this.action = action;
        }

        public PlaybackActionQueue.Action getAction() {
            return action;
        }
    }
}
