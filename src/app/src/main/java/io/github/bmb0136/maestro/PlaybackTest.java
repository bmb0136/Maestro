
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

        TimelineManager test_TimelineManager = starterManager();
        System.out.println("Basic Function Test");
        BasicFunction(test_TimelineManager);
        System.out.println("Basic Function Test: Completed");

        Thread.sleep(10000);

        System.out.println("Multi-Track Function Test");
        MultiTrackFunction(test_TimelineManager);

        System.out.println("Multi-Track Function Test: Completed");

        //engine.close();
    }
    public static TimelineManager starterManager(){
        TimelineManager manager = new TimelineManager(1024, new Timeline());
        Track track = new Track();
        manager.append(new AddTrackToTimelineEvent(track));
        Clip clip = PianoRollClip.create(0, 4);
        Clip clip2 = PianoRollClip.create(9, 4);
        manager.append(new AddClipToTrackEvent(track.getId(), clip));
        manager.append(new AddClipToTrackEvent(track.getId(), clip2));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip.getId(), new Note(new Pitch(PitchName.C_FLAT,6),0, 1)));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip.getId(), new Note(new Pitch(PitchName.C_FLAT,6),3, 1)));
        manager.append(new AddNoteToPianoRollClipEvent(
                track.getId(),clip2.getId(), new Note(new Pitch(PitchName.B_FLAT,6),0, 1)));
        manager.append (new AddNoteToPianoRollClipEvent(
                track.getId(), clip2.getId(), new Note(new Pitch(PitchName.D,6),(float) 3,1)));
        return manager;
    }
    public static void BasicFunction(TimelineManager manager) {
       play(manager);
    }
    //Testing the Ability to Play the Entire Timeline (Every Track synchronously)
    public static void MultiTrackFunction(TimelineManager manager) {
        Track track = new Track();

        manager.append(new AddTrackToTimelineEvent(track));
        Clip clip = PianoRollClip.create(0, 8);
        Clip clip2 = PianoRollClip.create(10, 8);
        manager.append(new AddClipToTrackEvent(track.getId(), clip));
        manager.append(new AddClipToTrackEvent(track.getId(), clip2));
        manager.append(new AddNoteToPianoRollClipEvent(track.getId(), clip.getId(),
                new Note(new Pitch(PitchName.A_SHARP,4),0, 6)));
        manager.append(new AddNoteToPianoRollClipEvent(track.getId(), clip2.getId(),
                new Note(new Pitch(PitchName.A_SHARP,4),4,4)));



        play(manager);
    }
    public static void play(TimelineManager manager) {
        try {

            //Testing The PlaybackEngine
            PlaybackEngine testPlayBack = new PlaybackEngine(manager);
            System.out.println("Starting Play Function");
            testPlayBack.play();
        }catch (Exception e){}
    }
}