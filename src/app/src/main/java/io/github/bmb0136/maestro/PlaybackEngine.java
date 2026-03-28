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
import java.util.concurrent.TimeUnit;


public class PlaybackEngine {

	private final TimelineManager manager;
	private Synthesizer synth;
	private MidiChannel[] channels;
	private int BPM = 120;

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
		MidiStarter();
	}

	/*
	Creation Notes:
	Point of MidiStarter:
		-Initalize The Synthesizer
		-Open Midi Channels for uses*

	 */
	public void MidiStarter() throws Exception {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			System.out.println("Using device: " + synth.getDeviceInfo().getName());
			if (synth.getAvailableInstruments().length > 0) {
				synth.loadInstrument(synth.getAvailableInstruments()[0]);
			}
			channels = synth.getChannels();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	Purpose: Creating ON/OFF Events from Notes List
	 */
	public final ArrayList<NoteEvent> buildIntoEvents(ArrayList<Note> notes) {
		long timeDuration = SecondstoBPM(10);
		ArrayList<NoteEvent> events = new ArrayList<>();

		for (Note not : notes) {
			double onTime = not.position() * timeDuration; //Logic: Position of Note * timeDuration = Start Position
			double offTime = (not.position() + not.duration()) * timeDuration; //Logic: (Position of StartPosition + The Expected duration of the Event) * Time according to BPM (timeDuration) = End Position
			//OnEvent : onEvents[i] =  new NoteEvent(NoteEvent.Type.ON, 0, not.pitch(),onTime);
			events.add(new NoteEvent(NoteEvent.Type.ON, not, not.pitch(), onTime));
			//OffEvent : offEvents[i] = new NoteEvent(NoteEvent.Type.OFF, 0, not.pitch(),offTime);

			events.add(new NoteEvent(NoteEvent.Type.OFF, not, not.pitch(), offTime));
		}
		//Sorts Events
		return NoteEventSorter(events);
		//EventPlayer - Schedules the Notes (Timeline Position, Player, Stop)


	}

	private ArrayList<NoteEvent> NoteEventSorter(ArrayList<NoteEvent> unsorted_events) {

		ArrayList<NoteEvent> sorted_events = new ArrayList<>(unsorted_events);
		sorted_events.sort(new NoteEventComparator());
		return sorted_events;
	}

	// Purpose: Work In Progress to handle Threading
	private void NoteEventScheduler2(ArrayList<NoteEvent> events) throws InterruptedException {
		System.out.println("NoteEventScheduler2");
		Runnable task;
		Thread thread;
		long startTime = System.nanoTime();
		MidiChannel Pianochannel = channels[0];
		Pianochannel.programChange(0);
		ArrayList<Thread> threads = new ArrayList<>(events.size());
		for (NoteEvent event : events) {
			task = () -> {
				long targetTime = startTime + TimeUnit.MILLISECONDS.toNanos((long) event.getTimeExecution());
				long delay = TimeUnit.MILLISECONDS.toMillis(Math.max(0, targetTime - System.nanoTime()));
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						System.out.print("NoteEventScheduler Interrupted");
					}
					int pitch = event.getNote().pitch().toMidi();
					int velocity = (int) event.getNote().volume();
					if (event.getType() == NoteEvent.Type.ON) {
						Pianochannel.noteOn(pitch, velocity);
						System.out.println("ON: " + event.toString());
					} else if (event.getType() == NoteEvent.Type.OFF) {
						Pianochannel.noteOff(pitch, velocity);
						System.out.println("OFF: " + event.toString());
					}

				}
			};
			try {
				String threadName =
						event.getType() == NoteEvent.Type.ON ? "On " : "Off ";
				threadName += event.getNote().pitch().name() + " - " +
						event.getTimeExecution() + "ms";
				thread = new Thread(task, threadName);
				thread.setName(threadName);
				System.out.println("Starting thread: " + threadName);
				thread.start();

			} catch (Exception e) {
				System.out.println("NoteEventScheduler interrupted");
				return;
			}
		}

		for (Thread threada : threads) {
			threada.join();
		}

	}

	private void NoteEventScheduler(ArrayList<NoteEvent> events, float Clip_position) throws InterruptedException {
		Runnable task;
		Thread thread;
		System.out.println("Begin NoteEventScheduler");
		long startTime = System.nanoTime();
		MidiChannel Pianochannel = channels[0];
		Pianochannel.programChange(0);
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
			float finalClipPosition = Clip_position;
			task = () -> {
				//System.out.println(event.getTimeExecution());
				long clipTime = startTime + TimeUnit.MILLISECONDS.toNanos((long) event.getTimeExecution())
						+ TimeUnit.MILLISECONDS.toNanos((long) (finalClipPosition * 1000));
				long delay = TimeUnit.NANOSECONDS.toMillis(Math.max(0, (((clipTime - startTime) * 60 )/ BPM)));
				System.out.println(delay);

				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						System.out.println("NoteEventScheduler interrupted");
						return;
					}
				}
				int pitch = event.getNote().pitch().toMidi();
				int velocity = (int) (event.getNote().volume() * 127);
				if (event.getType() == NoteEvent.Type.ON) {
					Pianochannel.noteOn(pitch, velocity);
					System.out.println("Note ON: " + event.getNote());

				} else if (event.getType() == NoteEvent.Type.OFF) {
					Pianochannel.noteOff(pitch, velocity);
					System.out.println("Note OFF: " + event.getNote());
				}
			};

			//To Name Each Split Thread; Great for Debugging!
			try {
				String threadName =
						event.getType() == NoteEvent.Type.ON ? "On " : "Off ";
				threadName += event.getNote().pitch().name() + " - " +
						event.getTimeExecution() + "ms";
				thread = new Thread(task, threadName);
				System.out.println("Starting thread: " + threadName);
				thread.start();

			} catch (Exception e) {
				System.out.println("NoteEventScheduler interrupted");
				return;
			}



		}
	}

	private final void playTimeline(Timeline timeline) {
		System.out.println("Playing timeline function: playTimeline");
		boolean running = false;
		for (Track track : timeline) {

			if (!running) {
				for (Clip clip : track) {
					running = true;
					ArrayList<Note> notes = new ArrayList<>();
					for (Note note : clip) {  //for Note : note in Clip
						notes.add(note);
					}
					//The Clip of Notes are then build into Events; Also Sorted during this
					ArrayList<NoteEvent> events = buildIntoEvents(notes);
					//Phase 2: Note Events are scheduled following their layout.
					try {
						//Changing The engine to include Clip position
						System.out.print(clip.getPosition());
						NoteEventScheduler(events, clip.getPosition());
						//NoteEventScheduler(events);
						running = false;
					} catch (InterruptedException e) {
						System.out.println("NoteEventScheduler interrupted");
					}

				}
			}
		}
	}

	/*
	Creation Notes: (Not Finished)
		Trent(March 23rd): Unnecessary, remove it in the future
		Trent(March 27th): Scratch that, will utilize later
		Point of playTrack:
			-To Play singular Tracks, instead of the entire timeline
			-Less Complicated version of Timeline; Just Play Notes -> Clips -> Track
	 */
	private final void playTrack(Track track) {
		try {
			for (Clip clip : track) {
				ArrayList<Note> notes = new ArrayList<>();
				for (Note note : clip) {  //for Note : note in Clip
					notes.add(note);
				}
				ArrayList<NoteEvent> events = buildIntoEvents(notes);
				try {
					NoteEventScheduler(events, clip.getPosition());
				} catch (InterruptedException e) {
					System.out.println("NoteEventScheduler interrupted");
				}

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
		System.out.println("Play Function: Ended");

	}

	public void stop() {
		if (synth != null) {
			synth.close();
			synth = null;
		}
	}

	//Useful, but sadly not for this project due to our focus on using only the Piano Instrument
	public void assignTrackstoChannels(Timeline timeline) {
		System.out.println("Assigning trackstoChannels");
		trackChannelMap.clear();
		int channel = 0;
		for (Track clips : timeline) { //Eh, Good Enough - Trent
			MidiChannel assignChannel = channels[channel];
			trackChannelMap.put(clips.getId(), assignChannel);
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
/*
class RunnableTask implements Runnable {
    private final NoteEvent noteEvent;
    public RunnableTask(NoteEvent noteEvent) {
        this.noteEvent = noteEvent;
    }


    public void run() {

    }
}*/


