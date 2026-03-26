package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.theory.Note;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;

import java.util.ArrayList;

import javax.sound.midi.MidiChannel; //Is this Required?
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import java.util.*;


public class PlaybackEngine {

    private final TimelineManager manager;
    private Synthesizer synth;
    private MidiChannel[] channels;

    //To Map the UUIDs to individal MIDI channels for the play session
    private Map<UUID, MidiChannel> trackChannelMap = new HashMap<>();

    /*
    -Breakdown (Trent's Thought Process):
        -The TimelineManger Holds Track Objects which should be played
        -The use of Thread is for Multithreading
        -Objective: Design a Playback Engine:
            -An algorithm that reads the TimelineManager, looks at the tracks contained
            -When a track is to be played, a Thread should split off to complete the command
                -Track Contains Multiple Types of Clips, and should be expected to know when they are/should be played
                -Multithreading is so the overall process of the Application isn't slowed down.

     */
    PlaybackEngine(TimelineManager timelineManager) throws Exception {
        System.out.println("PlaybackEngine created");
        this.manager = timelineManager;
        //Midistarter();
    }

    /*
    Creation Notes:
    Point of MidiStarter:
        -Initalize The Synthesizer
        -Open Midi Channels for uses*

     */
    public void Midistarter() throws Exception {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            System.out.println("Using device: " + synth.getDeviceInfo().getName());
            if (synth.getAvailableInstruments().length > 0) {
                synth.loadInstrument(synth.getAvailableInstruments()[0]);
            }
            channels = synth.getChannels();
            if (channels == null || channels.length > 0) {
                throw new IllegalStateException("Channels not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
       Most, if not all, of the code in has been used or initialized elsewhere.
        */
    public static void main(String[] args) throws Exception {

        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        System.out.println("Using device: " + synth.getDeviceInfo().getName());
        synth.loadInstrument(synth.getAvailableInstruments()[0]);
        MidiChannel[] channels = synth.getChannels();
        channels[0].noteOn(60, 127);
        Thread.sleep(1000);
        channels[0].noteOff(60);
        synth.close();
    }

    /*
    Purpose: Creating ON/OFF Events from Notes List
     */
    public final ArrayList<NoteEvent> buildIntoEvents(ArrayList<Note> notes) {
        long timeDuration = SecondstoBPM(60);
        ArrayList<NoteEvent> events = new ArrayList<>();
        // NoteEvent[] onEvents = new NoteEvent[channels.length];
        // NoteEvent[] offEvents = new NoteEvent[channels.length];
        int i = 0;
        for (Note not : notes) {
            double onTime = not.position() * timeDuration; //Logic: Position of Note * timeDuration = Start Position
            double offTime = (not.position() + not.duration()) * timeDuration; //Logic: (Position of StartPosition + The Expected duration of the Event) * Time according to BPM (timeDuration) = End Position
            //OnEvent : onEvents[i] =  new NoteEvent(NoteEvent.Type.ON, 0, not.pitch(),onTime);
            events.add(new NoteEvent(NoteEvent.Type.ON, not, not.pitch(), onTime));
            //OffEvent : offEvents[i] = new NoteEvent(NoteEvent.Type.OFF, 0, not.pitch(),offTime);
            events.add(new NoteEvent(NoteEvent.Type.OFF, not, not.pitch(), offTime));
        }
        System.out.println(events);
        //Sorts Events
        events = NoteEventSorter(events);
        //EventPlayer - Schedules the Notes (Timeline Position, Player, Stop)
        ArrayList<NoteEvent> properEvents = NoteEventScheduler(events);
        return properEvents;

    }

    private ArrayList<NoteEvent> NoteEventSorter(ArrayList<NoteEvent> unsorted_events) {

        ArrayList<NoteEvent> sorted_events = new ArrayList<>(unsorted_events);
        sorted_events.sort(new NoteEventComparator());
        for (NoteEvent event : sorted_events) {

            System.out.print(event.getNote().pitch());
            System.out.print(event.getType() +" " + event.getTimeExecution() +  " \n");
        }
        return sorted_events;
    }

    private ArrayList<NoteEvent> NoteEventScheduler(ArrayList<NoteEvent> events) {
        System.out.println("Begin NoteEventScheduler");
        long startTime = System.currentTimeMillis();
        MidiChannel Pianochannel = channels[0];
        /*
        Proper SetUp:
            -Main Thread
                -Grab ExecutionTime for each Event
                -Execute each event using Split Threads
                -The Main Program should continue on, each event should be executed via threads
            -
         */

        for (NoteEvent event : events) {
            //Worker Thread
            Runnable task = () -> {
                long targetTime = startTime + (long) event.getTimeExecution();
                long delay = targetTime - startTime;
                if (delay > 0){
                    try {
                        Thread.sleep(delay);
                    } catch(InterruptedException e){
                        System.out.println("NoteEventScheduler interrupted");
                    }
                }
                int pitch = 60;//event.getNote().pitch();
                int velocity = event.getPitch().octave();
                if (event.getType() == NoteEvent.Type.ON){
                    Pianochannel.noteOn(pitch, velocity);
                    System.out.println("Note ON: " + event.getNote());
                } else if (event.getType() == NoteEvent.Type.OFF){ //In case Variety
                    Pianochannel.noteOff(pitch, velocity);
                }
            };

            //To Name Each Split Thread; Great for Debugging!
            String threadName =
                    event.getType() == NoteEvent.Type.ON ? "on" : "off" +
                    event.getNote().toString() + "-" +
                    event.getTimeExecution() + "ms";
            Thread thread = new Thread(task);
            thread.setName(threadName);
            System.out.println("Starting thread: " + threadName);
            thread.start();


        }
        return events;
    }

    private final void playTimeline(Timeline timeline) {
        System.out.println("Playing timeline function: playTimeline");
        for (Track track : timeline) {
            int sad = 0;
            for (Clip clip : track) {
                ArrayList<Note> notes = new ArrayList<>();
               System.out.println(sad++);
                for (Note note : clip) {  //for Note : note in Clip
                    //Collect notes in a list; Need to know when to start/end.
                    notes.add(note);
                    System.out.println(note.pitch() + " " + note.position());
                    System.out.println("Note added");
                }

                ArrayList<NoteEvent> poke = buildIntoEvents(notes);
                // long startMS = SecondstoMillis(clip.getPosition());
                // long endMS = SecondstoMillis((clip.getDuration()));

            }
        }
    }

    /*
    Creation Notes: (Not Finished)
        Trent(March 23rd): Unnecessary, remove it in the future
        Point of playTrack:
            -To play tracks on separate Threads
            -(Not Really sure where to start)
            -Since The Tracks are Clips, we can just use Clips (I'm writing poorly)
     */
    private final void playTrack(Track track) {
        try {
            for (Clip clip : track) {
                //Executes Clip
                long startMS = SecondstoMillis(clip.getPosition());
                long endMS = SecondstoMillis(clip.getDuration());
                MidiChannel[] channels = synth.getChannels();
                int testnote = 60;
                channels[0].noteOn(testnote, 127);
                Thread.sleep(endMS);
                channels[0].noteOff(testnote);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Purpose: Starting the Playback of the current Timeline

     */
    public void play() {
        System.out.println("Play Function: Started");
        final Timeline asd = manager.get();
        //assignTrackstoChannels(asd);
        playTimeline(asd);
        long PlayStartNanos = System.nanoTime();
        System.out.println("Play Function: Ended");

    }

    public void stop() {
        if (synth != null) {
            synth.close();
            synth = null;
        }
    }

    public void assignTrackstoChannels(Timeline timeline) {
        System.out.println("Assigning trackstoChannels");
        trackChannelMap.clear();
        int channel = 0;
        Iterator<Track> Tracks = timeline.iterator();
        while (Tracks.hasNext()) { //Eh, Good Enough - Trent
            MidiChannel assignChannel = channels[channel];
            trackChannelMap.put(Tracks.next().getId(), assignChannel);

            //channel++;

        }

    }

    //Purpose: Consistency; simple way to convert systemmillis to Seconds
    private long MillistoSeconds() {
        return (long) (System.currentTimeMillis() / 1000);
    }

    //Purpose: Consistency; simple way to convert Seconds to systemMillis
    //NGL, this might be useless
    private long SecondstoMillis(float seconds) {
        return (long) (seconds * 1000.0f);
    }

    //Pretty sure I'll want this.
    private long SecondstoBPM(float seconds) {
        int temp_BPM = 100;
        return (long) seconds * temp_BPM;
    }


}
class RunnableTask implements Runnable {
    private final NoteEvent noteEvent;
    public RunnableTask(NoteEvent noteEvent) {
        this.noteEvent = noteEvent;
    }
    public void run() {

    }
}


