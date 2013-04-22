package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.view.helper.TickWindowHelper;

public class MidiManager implements Parcelable {
	private static MidiManager singletonInstance = null;

	public static final int MIN_BPM = 45;
	public static final int MAX_BPM = 300;
	// ticks per quarter note (I think)
	public static final int RESOLUTION = MidiFile.DEFAULT_RESOLUTION;
	public static final long TICKS_IN_ONE_MEASURE = RESOLUTION * 4;

	private static TimeSignature ts = new TimeSignature();
	private static Tempo tempo = new Tempo();
	private static MidiTrack tempoTrack = new MidiTrack();

	// stack of MidiNote lists, for undo
	private Stack<List<MidiNote>> undoStack = new Stack<List<MidiNote>>();

	private List<MidiNote> copiedNotes = new ArrayList<MidiNote>();
	private List<MidiNote> currState = new ArrayList<MidiNote>();

	public static long loopBeginTick, loopEndTick;

	private MidiManager() {
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		setBPM(120);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setLoopBeginTick(0);
		setLoopEndTick(RESOLUTION * 4);

		// saveState();
	}

	public static MidiManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new MidiManager();
		}
		return singletonInstance;
	}

	public float getBPM() {
		return tempo.getBpm();
	}

	public int setBPM(float bpm) {
		bpm = bpm >= MIN_BPM ? (bpm <= MAX_BPM ? bpm : MAX_BPM) : MIN_BPM;
		tempo.setBpm(bpm);
		setNativeBPM(bpm);
		setNativeMSPT(tempo.getMpqn() / RESOLUTION);
		Managers.trackManager.quantizeEffectParams();
		return (int) bpm;
	}

	public List<MidiNote> getMidiNotes() {
		List<MidiNote> midiNotes = new ArrayList<MidiNote>();
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			Track track = Managers.trackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				midiNotes.add(midiNote);
			}
		}
		return midiNotes;
	}

	public List<MidiNote> getSelectedNotes() {
		ArrayList<MidiNote> selectedNotes = new ArrayList<MidiNote>();
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			Track track = Managers.trackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				if (midiNote.isSelected()) {
					selectedNotes.add(midiNote);
				}
			}
		}
		return selectedNotes;
	}

	public void deselectAllNotes() {
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			Track track = Managers.trackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				deselectNote(midiNote);
			}
		}
	}

	// return true if any Midi note is selected
	public boolean anyNoteSelected() {
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			Track track = Managers.trackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				if (midiNote.isSelected()) {
					return true;
				}
			}
		}
		return false;
	}

	public void selectNote(MidiNote midiNote) {
		midiNote.setSelected(true);

		GlobalVars.mainPage.midiView.updateNoteFillColor(midiNote);
		updateEditIcons();
	}

	public void deselectNote(MidiNote midiNote) {
		midiNote.setSelected(false);
		GlobalVars.mainPage.midiView.updateNoteFillColor(midiNote);
		updateEditIcons();
	}

	public void selectRegion(long leftTick, long rightTick, int topNote,
			int bottomNote) {
		for (int i = topNote; i < bottomNote; i++) {
			Track track = Managers.trackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				// conditions for region selection
				boolean a = leftTick < midiNote.getOffTick();
				boolean b = rightTick > midiNote.getOffTick();
				boolean c = leftTick < midiNote.getOnTick();
				boolean d = rightTick > midiNote.getOnTick();
				if (a && b || c && d || !b && !c) {
					selectNote(midiNote);
				} else {
					deselectNote(midiNote);
				}
			}
		}
	}

	public void selectRow(int rowNum) {
		deselectAllNotes();
		for (MidiNote midiNote : Managers.trackManager.getTrack(rowNum)
				.getMidiNotes()) {
			selectNote(midiNote);
		}
	}

	public MidiNote addNote(long onTick, long offTick, int note,
			float velocity, float pan, float pitch) {
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
		Track track = Managers.trackManager.getTrack(midiNote.getNoteValue());
		track.addNote(midiNote);
		GlobalVars.mainPage.midiView.createNoteView(midiNote);
	}

	public void deleteNote(MidiNote midiNote) {
		midiNote.getRectangle().getGroup().remove(midiNote.getRectangle());
		Track track = Managers.trackManager.getTrack(midiNote.getNoteValue());
		track.removeNote(midiNote);
		updateEditIcons();
	}

	private void updateEditIcons() {
		boolean anyNoteSelected = anyNoteSelected();
		GlobalVars.mainPage.controlButtonGroup
				.setEditIconsEnabled(anyNoteSelected);
	}

	public void copy() {
		if (!anyNoteSelected())
			return;
		copiedNotes = copyMidiList(getSelectedNotes());
	}

	public boolean isCopying() {
		return !copiedNotes.isEmpty();
	}

	public void cancelCopy() {
		copiedNotes.clear();
	}

	public void paste(long startTick) {
		GlobalVars.mainPage.controlButtonGroup.uncheckCopyButton();
		if (copiedNotes.isEmpty())
			return;
		saveState();
		saveNoteTicks();
		deselectAllNotes();
		long tickOffset = startTick - getLeftMostTick(copiedNotes);
		for (MidiNote copiedNote : copiedNotes) {
			long newOnTick = copiedNote.getOnTick() + tickOffset;
			long newOffTick = copiedNote.getOffTick() + tickOffset;
			addNote(copiedNote);
			setNoteTicks(copiedNote, newOnTick, newOffTick, false, true);
			selectNote(copiedNote);
		}
		handleMidiCollisions();
		finalizeNoteTicks();
		copiedNotes.clear();
	}

	public void deleteSelectedNotes() {
		if (anyNoteSelected())
			saveState();
		for (MidiNote selected : getSelectedNotes()) {
			deleteNote(selected);
		}
	}

	public void clearNotes() {
		for (int i = 0; i < Managers.trackManager.getNumTracks(); i++) {
			Track track = Managers.trackManager.getTrack(i);
			while (!track.getMidiNotes().isEmpty()) {
				deleteNote(track.getMidiNotes().get(0)); // avoid concurrent mod
															// error
			}
		}
	}

	public boolean setNoteValue(MidiNote midiNote, int newNote) {
		int oldNote = midiNote.getNoteValue();
		if (oldNote == newNote)
			return false;
		midiNote.setNote(newNote);
		Track oldTrack = Managers.trackManager.getTrack(oldNote);
		Track newTrack = Managers.trackManager.getTrack(newNote);
		oldTrack.removeNote(midiNote);
		newTrack.addNote(midiNote);
		return true;
	}

	public boolean setNoteTicks(MidiNote midiNote, long onTick, long offTick,
			boolean snapToGrid, boolean maintainNoteLength) {
		if (midiNote.getOnTick() == onTick && midiNote.getOffTick() == offTick)
			return false;
		if (offTick <= onTick)
			offTick = onTick + 4;
		if (snapToGrid) {
			onTick = (long) TickWindowHelper.getMajorTickNearestTo(onTick);
			offTick = (long) TickWindowHelper.getMajorTickNearestTo(offTick) - 1;
			if (offTick == onTick - 1) {
				offTick += getTicksPerBeat(GlobalVars.currBeatDivision);
			}
		}
		if (maintainNoteLength)
			offTick = midiNote.getOffTick() + onTick - midiNote.getOnTick();
		Track track = Managers.trackManager.getTrack(midiNote.getNoteValue());
		return track.setNoteTicks(midiNote, onTick, offTick);
	}
	
	public Tempo getTempo() {
		return tempo;
	}

	public long getTicksPerBeat(float beatDivision) {
		return (long) (RESOLUTION / beatDivision);
	}

	public long millisToTick(long millis) {
		return (long) ((RESOLUTION * 1000f / tempo.getMpqn()) * millis);
	}

	public long getLeftMostTick(List<MidiNote> notes) {
		long leftMostTick = Long.MAX_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() < leftMostTick)
				leftMostTick = midiNote.getOnTick();
		}
		return leftMostTick;
	}

	public long getRightMostTick(List<MidiNote> notes) {
		long rightMostTick = Long.MIN_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOffTick() > rightMostTick)
				rightMostTick = midiNote.getOffTick();
		}
		return rightMostTick;
	}

	public long getLeftMostSelectedTick() {
		return getLeftMostTick(getSelectedNotes());
	}

	public long getRightMostSelectedTick() {
		return getRightMostTick(getSelectedNotes());
	}

	/*
	 * Translate the provided midi note to its on-tick's nearest major tick
	 * given the provided beat division
	 */
	public void quantize(MidiNote midiNote, float beatDivision) {
		long diff = (long) TickWindowHelper.getMajorTickNearestTo(midiNote
				.getOnTick()) - midiNote.getOnTick();
		setNoteTicks(midiNote, midiNote.getOnTick() + diff,
				midiNote.getOffTick() + diff, false, true);
	}

	public void saveNoteTicks() {
		for (MidiNote note : getMidiNotes()) {
			note.saveTicks();
		}
	}

	public void handleMidiCollisions() {
		for (int trackNum = 0; trackNum < Managers.trackManager.getNumTracks(); trackNum++) {
			Managers.trackManager.getTrack(trackNum).handleNoteCollisions();
		}
	}

	// called after release of touch event - this
	// finalizes the note on/off ticks of all notes
	public void finalizeNoteTicks() {
		for (MidiNote midiNote : getMidiNotes()) {
			if (midiNote.getOnTick() > TickWindowHelper.MAX_TICKS) {
				Log.e("MidiManager", "Deleting note in finalize!");
				deleteNote(midiNote);
			} else {
				midiNote.finalizeTicks();
			}
		}
	}
	
	/*
	 * Translate all midi notes to their on-ticks' nearest major ticks given the
	 * provided beat division
	 */
	public void quantize(float beatDivision) {
		for (MidiNote midiNote : getMidiNotes()) {
			quantize(midiNote, beatDivision);
		}
	}

	public void saveState() {
		undoStack.push(currState);
		currState = copyMidiList(getMidiNotes());
		// enforce max undo stack size
		if (undoStack.size() > GlobalVars.UNDO_STACK_SIZE)
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
		currState = copyMidiList(getMidiNotes());
	}

	private List<MidiNote> copyMidiList(List<MidiNote> midiList) {
		List<MidiNote> copy = new ArrayList<MidiNote>();
		for (int i = 0; i < midiList.size(); i++) {
			// avoid concurrent modification exception
			if (i < midiList.size()) {// note could have been added/deleted
										// after starting loop
				copy.add(midiList.get(i).getCopy());
			}
		}
		return copy;
	}

	public void writeToFile(File outFile) {
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
		midiTracks.add(tempoTrack);
		midiTracks.add(new MidiTrack());
		for (MidiNote midiNote : getMidiNotes()) {
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
			ts = (TimeSignature) tempoTrack.getEvents().get(0);
			tempo = (Tempo) tempoTrack.getEvents().get(1);
			setNativeMSPT(tempo.getMpqn() / RESOLUTION);
			ArrayList<MidiEvent> events = midiTracks.get(1).getEvents();
			clearNotes();
			// midiEvents are ordered by tick, so on/off events don't
			// necessarily
			// alternate if there are interleaving notes (with different "notes"
			// - pitches)
			// thus, we need to keep track of notes that have an on event, but
			// are waiting for the off event
			ArrayList<NoteOn> unfinishedNotes = new ArrayList<NoteOn>();
			for (int i = 0; i < events.size(); i++) {
				if (events.get(i) instanceof NoteOn)
					unfinishedNotes.add((NoteOn) events.get(i));
				else if (events.get(i) instanceof NoteOff) {
					NoteOff off = (NoteOff) events.get(i);
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
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeIntArray(new int[] { 4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION });
		out.writeFloat(tempo.getBpm());
		out.writeLong(Tempo.DEFAULT_MPQN / RESOLUTION);

		List<MidiNote> midiNotes = getMidiNotes();
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
		out.writeLong(getLoopBeginTick());
		out.writeLong(getLoopEndTick());
	}

	private MidiManager(Parcel in) {
		int[] timeSigInfo = new int[4];
		in.readIntArray(timeSigInfo);
		ts.setTimeSignature(timeSigInfo[0], timeSigInfo[1], timeSigInfo[2],
				timeSigInfo[3]);
		setBPM(in.readInt());
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setNativeMSPT(in.readLong());
		float[] noteInfo = new float[in.readInt()];
		in.readFloatArray(noteInfo);
		for (int i = 0; i < noteInfo.length; i += 4) {
			addNote((long) noteInfo[i], (long) noteInfo[i + 1],
					(int) noteInfo[i + 2], noteInfo[i + 3], noteInfo[i + 4],
					noteInfo[i + 5]);
		}
		setCurrTick(in.readLong());
		setLoopBeginTick(in.readLong());
		setLoopEndTick(in.readLong());
	}

	public void setLoopBeginTick(long loopBeginTick) {
		if (loopBeginTick >= loopEndTick)
			return;
		MidiManager.loopBeginTick = loopBeginTick;
		setLoopBeginTickNative(loopBeginTick);
	}

	public void setLoopEndTick(long loopEndTick) {
		if (loopEndTick <= loopBeginTick)
			return;
		MidiManager.loopEndTick = loopEndTick;
		setLoopEndTickNative(loopEndTick);
	}

	public void setLoopTicks(long loopBeginTick, long loopEndTick) {
		MidiManager.loopBeginTick = loopBeginTick;
		MidiManager.loopEndTick = loopEndTick;
		setLoopTicksNative(loopBeginTick, loopEndTick);
	}

	public long getLoopBeginTick() {
		return loopBeginTick;
	}

	public long getLoopEndTick() {
		return loopEndTick;
	}

	public native void isTrackPlaying(int trackNum);

	public native void setNativeMSPT(long MSPT);

	public native void setNativeBPM(float BPM);

	public native void reset();

	public native void setCurrTick(long currTick);

	public native long getCurrTick();

	public native void setLoopBeginTickNative(long loopBeginTick);

	public native void setLoopEndTickNative(long loopEndTick);

	public native void setLoopTicksNative(long loopBeginTick, long loopEndTick);

}
