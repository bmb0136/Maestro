package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;

import javax.sound.midi.MidiChannel; //Is this Required?
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


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
        Midistarter();
    }

    /*
    Creation Notes:
    Point of MidiStarter:
        -Initalize The Synthesizer
        -Open Midi Channels for uses*

     */
    public void Midistarter()throws Exception {
        try{
            synth = MidiSystem.getSynthesizer();
            synth.open();
            System.out.println("Using device: " + synth.getDeviceInfo().getName());
            if(synth.getAvailableInstruments().length > 0){
                synth.loadInstrument(synth.getAvailableInstruments()[0]);
            }
            channels = synth.getChannels();
            if(channels == null || channels.length > 0){
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
    Creation Notes: (Not Finished)
        Point of playTrack:
            -To play tracks on separate Threads
            -(Not Really sure where to start)
            -Since The Tracks are Clips, we can just use Clips (I'm writing poorly)
     */
    private final void playTrack(Track track){
        for (Clip clip : track){
            //Executes Clip
            clip.getPosition();
            clip.getDuration();
        }
    }
    /*
    Purpose: Starting the Playback of the current Timeline

     */
    public void play(){
        final Timeline asd = manager.get();
        assignTrackstoChannels(asd);
        long PlayStartNanos = System.nanoTime();
        Iterator<Track> Tracks = asd.iterator();
        while (Tracks.hasNext()) { //InComplete
            Track track = Tracks.next();
        }
    }
    public void stop(){
        if(synth != null){
            synth.close();
            synth = null;
        }
    }
    public void assignTrackstoChannels(Timeline timeline){
        //To Make sure The Mapping is clear
        trackChannelMap.clear();
        int channel = 0;
        Iterator<Track> Tracks = timeline.iterator();
        while (Tracks.hasNext()) { //Eh, Good Enough - Trent
            if(channel == channels.length || channels[channel] == null){
                throw new IllegalStateException("Channels not supported");
            }
            MidiChannel assignChannel = channels[channel];
            trackChannelMap.put(Tracks.next().getId(), assignChannel);

            channel++;

        }

    }
    //Purpose: Consistency; simple way to convert systemmillis to Seconds
    private  long MillistoSeconds(){
        return (long) (System.currentTimeMillis()/1000);
    }
    //Purpose: Consistency; simple way to convert Seconds to systemMillis
        //NGL, this might be useless
    private  long SecondstoMillis(float seconds){
        return (long) (seconds * 1000.0f);
    }


}


