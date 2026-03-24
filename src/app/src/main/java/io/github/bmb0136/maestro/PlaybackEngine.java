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
            events.add(new NoteEvent(NoteEvent.Type.ON, 0, not.pitch(), onTime));
            //OffEvent : offEvents[i] = new NoteEvent(NoteEvent.Type.OFF, 0, not.pitch(),offTime);
            events.add(new NoteEvent(NoteEvent.Type.OFF, 0, not.pitch(), offTime));
        }
        //Sorts Events
        events = NoteEventSorter(events);
        //EventPlayer - Schedules the Notes (Timeline Position, Player, Stop)
        ArrayList<NoteEvent> properEvents = NoteEventScheduler(events);
        return properEvents;

    }

    private ArrayList<NoteEvent> NoteEventSorter(ArrayList<NoteEvent> unsorted_events) {
        //Temp Sorter; Don't like the code for this (Also Unfinished, needs Comparator)
        ArrayList<NoteEvent> sorted_events = new ArrayList<>();
        sorted_events.addAll(unsorted_events);
        sorted_events.sort(new NoteEventComparator());
        //sorted_events.sort();
        return sorted_events;
    }

    private ArrayList<NoteEvent> NoteEventScheduler(ArrayList<NoteEvent> events) {
        double currentTime = 0;
        for (NoteEvent event : events) {
            double waitTime = event.getTimeExecution() - currentTime;
            currentTime = event.getTimeExecution();
            try {
                Thread.sleep((long) waitTime * 1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return events;
    }

    private final void playTimeline(Timeline timeline) {
        System.out.println("Playing timeline function: playTimeline");
        for (Track track : timeline) {
            for (Clip clip : track) {
                ArrayList<Note> notes = new ArrayList<>();
                int sad = 0;
                for (Note note : clip) {  //for Note : note in Clip
                    //Collect notes in a list; Need to know when to start/end.
                    notes.add(note);
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


