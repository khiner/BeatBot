package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.os.Parcel;
import android.os.Parcelable;

import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;

public class MidiManager implements Parcelable {

	private static MidiManager singletonInstance = null;
	
	private TimeSignature ts = new TimeSignature();
	private Tempo tempo = new Tempo();
	private MidiTrack tempoTrack = new MidiTrack();
	private List<MidiNote> midiNotes = new ArrayList<MidiNote>();
	// if a note is dragged over another, the "eclipsed" note should be
	// shortened or removed as appropriate. However, these changes only become
	// saved to the midiManager.midiNotes list after the eclipsing note is
	// "dropped". If the note is dragged off of the eclipsed note, the original
	// note is used again instead of its temp version
	// the integer keys correspond to indices in midiManager.midiNotes list
	private Map<Integer, MidiNote> tempNotes = new HashMap<Integer, MidiNote>();
	
	// stack of MidiNote lists, for undo
	private Stack<List<MidiNote>> undoStack = new Stack<List<MidiNote>>();

	private List<MidiNote> currState = new ArrayList<MidiNote>();
	
	private Thread tickThread = null;
	private PlaybackManager playbackManager = null;
	private RecordManager recordManager = null;

	private MidiEvent lastEvent = null;
	private boolean recording;
	private int numSamples = 0;

	// ticks per quarter note (I think)
	public final int RESOLUTION = MidiFile.DEFAULT_RESOLUTION;

	private long currTick = -1;
	private long loopTick = RESOLUTION * 4;
	private long MSPT;

	private MidiManager(int numSamples) {
		this.numSamples = numSamples;

		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		tempo.setBpm(140);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		MSPT = tempo.getMpqn() / RESOLUTION;
		saveState();
	}
	
	public static MidiManager getInstance(int numSamples) {
		if (singletonInstance == null) {
			singletonInstance = new MidiManager(numSamples);
		}
		return singletonInstance;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	public float getBPM() {
		return tempo.getBpm();
	}

	public void setBPM(float bpm) {
		tempo.setBpm(bpm);
		MSPT = tempo.getMpqn() / RESOLUTION;
	}

	public int getNumSamples() {
		return numSamples;
	}


	public List<MidiNote> getMidiNotes() {
		return midiNotes;
	}

	public MidiNote getMidiNote(int i) {
		// if there is a temporary (clipped or deleted) version of the note,
		// return that version instead
		return tempNotes.keySet().contains(i) ? tempNotes.get(i) : midiNotes
				.get(i);
	}

	public Map<Integer, MidiNote> getTempNotes() {
		return tempNotes;
	}

	public void mergeTempNotes() {
		for (int k : tempNotes.keySet()) {
			if (k < midiNotes.size()) {// sanity check
				if (tempNotes.get(k) != null)
					midiNotes.set(k, tempNotes.get(k));
				else
					midiNotes.remove(k);
			}
		}
		tempNotes.clear();
	}

	public long getLoopTick() {
		return loopTick;
	}

	public void setLoopTick(long loopTick) {
		this.loopTick = loopTick;
	}

	public MidiNote addNote(long onTick, long offTick, int note, int velocity) {
		NoteOn on = new NoteOn(onTick, 0, note, velocity);
		NoteOff off = new NoteOff(offTick, 0, note, velocity);
		MidiNote midiNote = new MidiNote(on, off);
		midiNotes.add(midiNote);
		return midiNote;
	}

	public void removeNote(MidiNote midiNote) {
		if (midiNotes.contains(midiNote)) {
			midiNotes.remove(midiNote);
		}
	}

	public Tempo getTempo() {
		return tempo;
	}

	public long getLastTick() {
		return lastEvent.getTick();
	}

	/*
	 * Translate the provided midi note to its on-tick's nearest major tick
	 * given the provided beat division
	 */
	public void quantize(MidiNote midiNote, float beatDivision) {
		long ticksPerBeat = (long) (RESOLUTION / beatDivision);
		long diff = midiNote.getOnTick() % ticksPerBeat;
		// is noteOn closer to left or right tick?
		diff = diff <= ticksPerBeat / 2 ? -diff : ticksPerBeat - diff;
		midiNote.setOnTick(midiNote.getOnTick() + diff);
		midiNote.setOffTick(midiNote.getOffTick() + diff);
	}

	/*
	 * Translate all midi notes to their on-ticks' nearest major ticks given the
	 * provided beat division
	 */
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
		long startNano = System.nanoTime();
		long nextTickNano = startNano + MSPT * 1000;

		while (playbackManager.getState() == PlaybackManager.State.PLAYING
				|| recordManager.getState() != RecordManager.State.INITIALIZING) {
			if (System.nanoTime() >= nextTickNano) {
				currTick++;
				if (currTick >= loopTick) {
					playbackManager.stopAllSamples();
					if (recording) {
						try {
							recordManager.stopRecording();
							currTick = 0;
							recordManager.startRecording();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else
						currTick = 0;
				}
				for (int i = 0; i < midiNotes.size(); i++) {
					MidiNote midiNote = getMidiNote(i);
					// note(s) could have been deleted since the start of the
					// loop
					if (midiNote == null)
						continue;
					if (currTick == midiNote.getOnTick())
						// note - 1, since track 0 is recording track
						playbackManager.playSample(midiNote.getNoteValue() - 1, midiNote.getVelocity());
					else if (currTick == midiNote.getOffTick())
						playbackManager.stopSample(midiNote.getNoteValue() - 1);
				}
				// update time of next tick
				nextTickNano += MSPT * 1000;
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

	public void reset() {
		currTick = -1;
	}

	public void setRecordNoteOn(int velocity) {
		long onTick = currTick;
		// tick, channel, note, velocity
		NoteOn on = new NoteOn(onTick, 0, 0, velocity);
		lastEvent = on;
		recording = true;

	}

	public void setRecordNoteOff(int velocity) {
		long offTick = currTick;
		// tick, channel, note, velocity
		NoteOff off = new NoteOff(offTick, 0, 0, velocity);
		midiNotes.add(new MidiNote((NoteOn)lastEvent, off));
		lastEvent = off;
		recording = false;
	}

	public long getCurrTick() {
		return currTick;
	}

	public void saveState() {
		undoStack.push(currState);
		currState = copyMidiList(midiNotes);
		// max undo stack of 40
		if (undoStack.size() > 40)
			undoStack.remove(0);
	}
	
	public void undo() {
		if (undoStack.isEmpty())
			return;
		List<MidiNote> lastState = undoStack.pop();
		midiNotes = lastState;
		currState = copyMidiList(midiNotes);
	}
	
	private List<MidiNote> copyMidiList(List<MidiNote> midiList) {
		List<MidiNote> copy = new ArrayList<MidiNote>();
		for (MidiNote midiNote : midiNotes) {
			copy.add(midiNote.getCopy());
		}
		return copy;
	}
	
	public void writeToFile(File outFile) {
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
		midiTracks.add(tempoTrack);
		midiTracks.add(new MidiTrack());
		for (MidiNote midiNote : midiNotes) {
			midiTracks.get(1).insertEvent(midiNote.getOnEvent());
			midiTracks.get(1).insertEvent(midiNote.getOffEvent());
		}
		Collections.sort(midiTracks.get(1).getEvents());
		midiTracks.get(1).recalculateDeltas();

		MidiFile midi = new MidiFile(RESOLUTION, midiTracks);

		// 4. Write the MIDI data to a file
		try {
			midi.writeToFile(outFile);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public void importFromFile(FileInputStream in) {
		try {
			MidiFile midiFile = new MidiFile(in);
			ArrayList<MidiTrack> midiTracks = midiFile.getTracks();
			tempoTrack = midiTracks.get(0);
			ArrayList<MidiEvent> events = midiTracks.get(1).getEvents();
			midiNotes = new ArrayList<MidiNote>();
			// midiEvents are ordered by tick, so on/off events don't necessarily
			// alternate if there are interleaving notes (with different "notes" - pitches)
			// thus, we need to keep track of notes that have an on event, but are waiting for the off event
			ArrayList<NoteOn> unfinishedNotes = new ArrayList<NoteOn>();
			for (int i = 0; i < events.size(); i++) {
				if (events.get(i) instanceof NoteOn)
					unfinishedNotes.add((NoteOn)events.get(i));
				else if (events.get(i) instanceof NoteOff) {
					NoteOff off = (NoteOff)events.get(i);
					for (int j = 0; j < unfinishedNotes.size(); j++) {
						NoteOn on = unfinishedNotes.get(j);
						if (on.getNoteValue() == off.getNoteValue()) {
							midiNotes.add(new MidiNote(on, off));
							unfinishedNotes.remove(j);
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(numSamples);
		out.writeIntArray(new int[] { 4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION });
		out.writeFloat(tempo.getBpm());
		out.writeLong(Tempo.DEFAULT_MPQN / MidiFile.DEFAULT_RESOLUTION);
		// on-tick, off-tick, note, and velocity for each midiNote
		long[] noteInfo = new long[midiNotes.size() * 4];
		for (int i = 0; i < midiNotes.size(); i++) {
			noteInfo[i * 4] = midiNotes.get(i).getOnTick();
			noteInfo[i * 4 + 1] = midiNotes.get(i).getOffTick();
			noteInfo[i * 4 + 2] = (long) midiNotes.get(i).getNoteValue();
			noteInfo[i * 4 + 3] = (long) midiNotes.get(i).getVelocity();
		}
		out.writeInt(noteInfo.length);
		out.writeLongArray(noteInfo);
		out.writeLong(currTick);
		out.writeLong(loopTick);
	}

	private MidiManager(Parcel in) {
		numSamples = in.readInt();
		int[] timeSigInfo = new int[4];
		in.readIntArray(timeSigInfo);
		ts.setTimeSignature(timeSigInfo[0], timeSigInfo[1], timeSigInfo[2],
				timeSigInfo[3]);
		tempo.setBpm(in.readInt());
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		MSPT = in.readLong();
		long[] noteInfo = new long[in.readInt()];
		in.readLongArray(noteInfo);
		for (int i = 0; i < noteInfo.length; i += 4) {
			addNote(noteInfo[i], noteInfo[i + 1], (int) noteInfo[i + 2], (int)noteInfo[i + 3]);
		}
		currTick = in.readLong();
		loopTick = in.readLong();
	}
}
