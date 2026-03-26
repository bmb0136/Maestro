
package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.AddClipToTrackEvent;
import io.github.bmb0136.maestro.core.event.AddNoteToPianoRollClipEvent;
import io.github.bmb0136.maestro.core.event.AddTrackToTimelineEvent;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.theory.Pitch;
import io.github.bmb0136.maestro.core.theory.PitchName;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;

public class PlaybackTest {
    public static void main(String[] args) throws InterruptedException {
        TimelineManager manager = new TimelineManager(1024, new Timeline());
        Track track = new Track();

        manager.append(new AddTrackToTimelineEvent(track));
        Clip clip = PianoRollClip.create(0, 4);
        Clip clip2 = PianoRollClip.create(10, 4);
        manager.append(new AddClipToTrackEvent(track.getId(), clip));
        manager.append(new AddClipToTrackEvent(track.getId(), clip2));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip.getId(), new Note(new Pitch(PitchName.C_FLAT,6),0, 2)));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip.getId(), new Note(new Pitch(PitchName.C_FLAT,6),2, 2)));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip2.getId(), new Note(new Pitch(PitchName.B_FLAT,6),0, 2)));
        manager.append (new AddNoteToPianoRollClipEvent(
                track.getId(), clip2.getId(), new Note(new Pitch(PitchName.D,6),(float)1.5,1)));


        try {
            //Testing The PlaybackEngine
            PlaybackEngine testPlayBack = new PlaybackEngine(manager);
            System.out.println("Starting Play Function");
            testPlayBack.play();
        }catch (Exception e){}
        Note[] notes = {/*  */};
        for (var n : notes) {
            //    manager.append(new AddNoteToPianoRollClipEvent(track.getId(), clip.getId(), n));
        }


        Thread.sleep(5000);
        //engine.close();
    }
}