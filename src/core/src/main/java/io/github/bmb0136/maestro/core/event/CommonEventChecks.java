package io.github.bmb0136.maestro.core.event;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Track;
import org.jetbrains.annotations.NotNull;

final class CommonEventChecks {
    private CommonEventChecks() {}

    public static boolean doesClipOverlapExisting(@NotNull Track track, @NotNull Clip clip) {
        for (Clip other : track) {
            if (other.getId().equals(clip.getId())) {
                continue;
            }
            float left = Math.max(clip.getPosition(), other.getPosition());
            float right = Math.min(clip.getPosition() + clip.getDuration(), other.getPosition() + other.getDuration());
            float overlap = right - left;
            if (overlap > 1e-6f) {
                return true;
            }
        }
        return false;
    }
}
