package com.kh.beatbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.os.Environment;
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

	private static final String SAVE_FOLDER = "BeatBot/MIDI";
	private TimeSignature ts = new TimeSignature();
	private Tempo tempo = new Tempo();
	private MidiTrack tempoTrack = new MidiTrack();
	private ArrayList<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
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

	private boolean recording = false;
	private int numSamples = 0;

	// ticks per quarter note (I think)
	public final int RESOLUTION = MidiFile.DEFAULT_RESOLUTION;

	private long currTick = -1;
	private long loopTick = RESOLUTION * 4;
	private long MSPT;

	public MidiManager(int numSamples) {
		this.numSamples = numSamples;

		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		tempo.setBpm(140);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		MSPT = tempo.getMpqn() / MidiFile.DEFAULT_RESOLUTION;
		midiTracks.add(new MidiTrack());
		saveState();
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
		MSPT = tempo.getMpqn() / MidiFile.DEFAULT_RESOLUTION;
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

	public MidiNote addNote(long onTick, long offTick, int note) {
		NoteOn on = new NoteOn(onTick, 0, note, 100);
		NoteOff off = new NoteOff(offTick, 0, note, 100);
		midiTracks.get(0).insertEvent(on);
		midiTracks.get(0).insertEvent(off);
		MidiNote midiNote = new MidiNote(on, off);
		midiNotes.add(midiNote);
		return midiNote;
	}

	public void removeNote(MidiNote midiNote) {
		if (midiNotes.contains(midiNote)) {
			midiTracks.get(0).removeEvent(midiNote.getOnEvent());
			midiTracks.get(0).removeEvent(midiNote.getOffEvent());
			midiNotes.remove(midiNote);
		}
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
				nextTickNano += MSPT * 1000;
				for (int i = 0; i < midiNotes.size(); i++) {
					MidiNote midiNote = getMidiNote(i);
					// note(s) could have been deleted since the start of the
					// loop
					if (midiNote == null)
						continue;
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

	public void reset() {
		currTick = -1;
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
	
	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".MIDI");
	}

	public void writeToFile() {
		// 3. Create a MidiFile with the tracks we created
		for (MidiTrack midiTrack : midiTracks) {
			Collections.sort(midiTrack.getEvents());
		}
		midiTracks.add(0, tempoTrack);

		MidiFile midi = new MidiFile(RESOLUTION, midiTracks);

		// 4. Write the MIDI data to a file
		File output = new File(getFilename());
		try {
			midi.writeToFile(output);
		} catch (IOException e) {
			System.err.println(e);
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
		// on-tick, off-tick, and note for each midiNote
		long[] noteInfo = new long[midiNotes.size() * 3];
		for (int i = 0; i < midiNotes.size(); i++) {
			noteInfo[i * 3] = midiNotes.get(i).getOnTick();
			noteInfo[i * 3 + 1] = midiNotes.get(i).getOffTick();
			noteInfo[i * 3 + 2] = (long) midiNotes.get(i).getNote();
		}
		out.writeInt(noteInfo.length);
		out.writeLongArray(noteInfo);
		out.writeLong(currTick);
		out.writeLong(loopTick);
	}

	public static final Parcelable.Creator<MidiManager> CREATOR = new Parcelable.Creator<MidiManager>() {
		public MidiManager createFromParcel(Parcel in) {
			return new MidiManager(in);
		}

		public MidiManager[] newArray(int size) {
			return new MidiManager[size];
		}
	};

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
		midiTracks.add(new MidiTrack());
		long[] noteInfo = new long[in.readInt()];
		in.readLongArray(noteInfo);
		for (int i = 0; i < noteInfo.length; i += 3) {
			addNote(noteInfo[i], noteInfo[i + 1], (int) noteInfo[i + 2]);
		}
		currTick = in.readLong();
		loopTick = in.readLong();
	}
}
