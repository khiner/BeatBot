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
import android.util.Log;

import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.view.helper.LevelsViewHelper;

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

	private int numSamples = 0;

	// ticks per quarter note (I think)
	public final int RESOLUTION = MidiFile.DEFAULT_RESOLUTION;

	private MidiManager(int numSamples) {
		this.numSamples = numSamples;
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		tempo.setBpm(140);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setMSPT(tempo.getMpqn() / RESOLUTION);
		setLoopTick(RESOLUTION * 4);		
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
		setMSPT(tempo.getMpqn() / RESOLUTION);
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
				if (tempNotes.get(k) != null) {
					replaceNote(k, tempNotes.get(k));					
				}
				else {
					removeNote(midiNotes.get(k));
				}
			}
		}
		tempNotes.clear();
	}

	public MidiNote addNote(long onTick, long offTick, int note, float velocity, float pan, float pitch) {
		NoteOn on = new NoteOn(onTick, 0, note, velocity, pan, pitch);
		NoteOff off = new NoteOff(offTick, 0, note, velocity, pan, pitch);
		return addNote(on, off);			
	}
	
	private MidiNote addNote(NoteOn on, NoteOff off) {
		MidiNote midiNote = new MidiNote(on, off);
		addNote(midiNote);
		return midiNote;		
	}
	
	private void addNote(MidiNote midiNote) {
		midiNotes.add(midiNote);
		addMidiEvents(midiNote.getNoteValue(), midiNote.getOnTick(),
				midiNote.getOffTick(), midiNote.getVelocity(), midiNote.getPan(), midiNote.getPitch());
	}
	
	public void removeNote(MidiNote midiNote) {
		if (midiNotes.contains(midiNote)) {
			midiNotes.remove(midiNote);
			removeMidiEvents(midiNote.getNoteValue(), midiNote.getOnTick(), midiNote.getOffTick());
		}
	}

	public void clearNotes() {
		for (MidiNote midiNote : midiNotes) {
			removeMidiEvents(midiNote.getNoteValue(), midiNote.getOnTick(), midiNote.getOffTick());
		}
		midiNotes.clear();
	}
	public void replaceNote(int pos, MidiNote newNote) {
		MidiNote oldNote = midiNotes.get(pos);
		if (oldNote == null)
			return;
		Log.d("replace", "replace");
		removeMidiEvents(oldNote.getNoteValue(), oldNote.getOnTick(), oldNote.getOffTick());
		addMidiEvents(newNote.getNoteValue(), newNote.getOnTick(), newNote.getOffTick(),
				newNote.getVelocity(), newNote.getPan(), newNote.getPitch());
		midiNotes.set(pos, newNote);
	}
	
	public void setNoteValue(MidiNote midiNote, int newNote) {
		if (midiNote.getNoteValue() == newNote)
			return;
		moveMidiEventNote(midiNote.getNoteValue(), midiNote.getOnTick(), midiNote.getOffTick(), newNote);
		midiNote.setNote(newNote);		
	}
	
	public void setNoteTicks(MidiNote midiNote, long onTick, long offTick) {
		if (midiNote.getOnTick() == onTick && midiNote.getOffTick() == offTick)
			return;
		moveMidiEventTicks(midiNote.getNoteValue(), midiNote.getOnTick(), onTick, midiNote.getOffTick(), offTick);
		midiNote.setOnTick(onTick);
		midiNote.setOffTick(offTick);		
	}

	public Tempo getTempo() {
		return tempo;
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
		setNoteTicks(midiNote, midiNote.getOnTick() + diff, midiNote.getOffTick() + diff);
	}

	/*
	 * Translate all midi notes to their on-ticks' nearest major ticks given the
	 * provided beat division
	 */
	public void quantize(float beatDivision) {
		for (MidiNote midiNote : midiNotes) {
			quantize(midiNote, beatDivision);
		}
	}

	public void start() {
		new Thread() {
			public void run() {
				startTicking();
			}
		}
		.start();		
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
		clearNotes();
		for (MidiNote midiNote : lastState) {
			addNote(midiNote);
		}
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
			ts = (TimeSignature)tempoTrack.getEvents().get(0);
			tempo = (Tempo)tempoTrack.getEvents().get(1);
			setMSPT(tempo.getMpqn() / RESOLUTION);
			ArrayList<MidiEvent> events = midiTracks.get(1).getEvents();
			clearNotes();
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
							addNote(on, off);
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
		float[] noteInfo = new float[midiNotes.size() * 6];
		for (int i = 0; i < midiNotes.size(); i++) {
			noteInfo[i * 6] = midiNotes.get(i).getOnTick();
			noteInfo[i * 6 + 1] = midiNotes.get(i).getOffTick();
			noteInfo[i * 6 + 2] = midiNotes.get(i).getNoteValue();
			noteInfo[i * 6 + 3] = midiNotes.get(i).getVelocity();
			noteInfo[i * 6 + 4] = midiNotes.get(i).getPan();
			noteInfo[i * 6 + 5] = midiNotes.get(i).getPitch();
		}
		out.writeInt(noteInfo.length);
		out.writeFloatArray(noteInfo);
		out.writeLong(getCurrTick());
		out.writeLong(getLoopTick());
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
		setMSPT(in.readLong());
		float[] noteInfo = new float[in.readInt()];
		in.readFloatArray(noteInfo);
		for (int i = 0; i < noteInfo.length; i += 4) {
			addNote((long)noteInfo[i], (long)noteInfo[i + 1], (int)noteInfo[i + 2], noteInfo[i + 3], noteInfo[i + 4], noteInfo[i + 5]);
		}
		setCurrTick(in.readLong());
		setLoopTick(in.readLong());
	}
	
	public native void setMSPT(long MSPT);
	public native void reset();
	public native void setCurrTick(long currTick);
	public native long getCurrTick();
	public native long getLoopTick();
	public native void setLoopTick(long loopTick);
	
	public native void startTicking();
	
	public native void addMidiEvents(int track, long onTick, long offTick,
										float volume, float pan, float pitch);
	public native void removeMidiEvents(int track, long onTick, long offTick);
	// change ticks
	public native void moveMidiEventTicks(int track, long prevOnTick, long newOnTick, long prevOffTick, long newOffTick);
	// change track num
	public native void moveMidiEventNote(int track, long onTick, long offTick, int newTrack);
}
