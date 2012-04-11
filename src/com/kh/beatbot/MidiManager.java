package com.kh.beatbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;

public class MidiManager {

	TimeSignature ts = new TimeSignature();
	Tempo tempo = new Tempo();
	MidiTrack tempoTrack = new MidiTrack();
	List<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
	List<MidiNote> midiNotes = new ArrayList<MidiNote>();

	Thread tickThread = null;
	PlaybackManager playbackManager = null;
	RecordManager recordManager = null;

	boolean recording = false;
	int numSamples = 0;

	// ticks per quarter note (I think)
	public final int RESOLUTION = MidiFile.DEFAULT_RESOLUTION;

	long startNanoTime = -1;
	long currTick = 0;
	long MSPT;

	public MidiManager(int numSamples) {
		this.numSamples = numSamples;

		midiTracks.add(new MidiTrack());
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		tempo.setBpm(140);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		MSPT = Tempo.DEFAULT_MPQN / MidiFile.DEFAULT_RESOLUTION;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public List<MidiTrack> getMidiTracks() {
		return midiTracks;
	}

	public List<MidiNote> getMidiNotes() {
		return midiNotes;
	}

	public void addNote(long onTick, long offTick, int note) {
		NoteOn on = new NoteOn(onTick, 0, note, 100);
		NoteOff off = new NoteOff(offTick, 0, note, 100);
		midiNotes.add(new MidiNote(on, off));
	}

	public void removeNote(MidiNote midiNote) {
		if (midiNotes.contains(midiNote))
			midiNotes.remove(midiNote);
	}

	public Tempo getTempo() {
		return tempo;
	}

	public MidiEvent getLastEvent() {
		return midiTracks.get(0).getEvents()
				.get(midiTracks.get(0).getEventCount() - 1);
	}

	public long getLastTick() {
		return midiTracks.get(0).getEvents()
				.get(midiTracks.get(0).getEventCount() - 1).getTick();
	}

	public void quantize(MidiNote midiNote, float beatDivision) {
		long ticksPerBeat = (long) (RESOLUTION / beatDivision);
		long diff = midiNote.getOnTick() % ticksPerBeat;
		// is noteOn closer to left or right tick?
		diff = diff <= ticksPerBeat / 2 ? -diff : ticksPerBeat - diff;
		midiNote.setOnTick(midiNote.getOnTick() + diff);
		midiNote.setOffTick(midiNote.getOffTick() + diff);
	}

	public void quantize(float beatDivision) {
		long ticksPerBeat = (long) (RESOLUTION / beatDivision);
		for (MidiNote midiNote : midiNotes) {
			long diff = midiNote.getOnTick() % ticksPerBeat;
			// is noteOn closer to left or right tick?
			diff = diff <= ticksPerBeat / 2 ? -diff : ticksPerBeat - diff;
			midiNote.setOnTick(midiNote.getOnTick() + diff);
			midiNote.setOffTick(midiNote.getOffTick() + diff);
		}
	}

	public boolean isRecording() {
		return recording;
	}

	public void runTicker() {
		currTick = -1;
		long startNano = System.nanoTime();
		long nextTickNano = startNano + MSPT * 1000;

		while (playbackManager.getState() == PlaybackManager.State.PLAYING
				|| recordManager.getState() == RecordManager.State.RECORDING
				|| recordManager.getState() == RecordManager.State.LISTENING) {
			if (System.nanoTime() >= nextTickNano) {
				currTick++;
				nextTickNano += MSPT * 1000;
				for (int i = 0; i < midiNotes.size(); i++) {
					MidiNote midiNote = midiNotes.get(i);
					// note(s) could have been deleted since the start of the
					// loop
					if (midiNote == null)
						break;
					if (currTick == midiNote.getOnTick())
						// note - 1, since track 0 is recording track
						playbackManager.playSample(midiNote.getNote() - 1);
					else if (currTick == midiNote.getOffTick())
						playbackManager.stopSample(midiNote.getNote() - 1);
				}
			}
		}
	}

	public void start() {
		tickThread = new Thread(new Runnable() {
			@Override
			public void run() {
				runTicker();
			}
		}, "Tick Thread");
		tickThread.start();
	}

	public void setRecordNoteOn(int velocity) {
		long onTick = currTick;
		// tick, channel, note, velocity
		NoteOn on = new NoteOn(onTick, 0, 0, velocity);
		midiTracks.get(0).insertEvent(on);
		recording = true;
	}

	public void setRecordNoteOff(int velocity) {
		long offTick = currTick;
		// tick, channel, note, velocity
		NoteOff off = new NoteOff(offTick, 0, 0, velocity);
		midiNotes.add(new MidiNote((NoteOn) getLastEvent(), off));
		midiTracks.get(0).insertEvent(off);
		recording = false;
	}

	public long getCurrTick() {
		return currTick;
	}

	// public long nanoToTick(long nanoTime) {
	// if (startNanoTime == -1) {
	// return 0;
	// }
	// float microSec = (nanoTime - startNanoTime)/1000;
	// float quarterNotes = microSec/tempo.getMpqn();
	// return (long)(quarterNotes*RESOLUTION);
	// }

	public void writeToFile() {
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		for (MidiTrack midiTrack : midiTracks) {
			Collections.sort(midiTrack.getEvents());
			tracks.add(midiTrack);
		}

		MidiFile midi = new MidiFile(RESOLUTION, tracks);

		// 4. Write the MIDI data to a file
		File output = new File("data/midi/test.mid");
		try {
			midi.writeToFile(output);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
