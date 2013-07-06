package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.util.Log;

import com.kh.beatbot.GlobalVars;
import com.kh.beatbot.Track;
import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class MidiManager {

	public static final int MIN_BPM = 45, MAX_BPM = 300,
	// ticks per quarter note (I think)
			RESOLUTION = MidiFile.DEFAULT_RESOLUTION;

	public static final long TICKS_IN_ONE_MEASURE = RESOLUTION * 4;

	private static TimeSignature ts = new TimeSignature();
	private static Tempo tempo = new Tempo();
	private static MidiTrack tempoTrack = new MidiTrack();

	// stack of MidiNote lists, for undo
	private static Stack<List<MidiNote>> undoStack = new Stack<List<MidiNote>>();

	private static List<MidiNote> copiedNotes = new ArrayList<MidiNote>(),
			currState = new ArrayList<MidiNote>();

	public static long loopBeginTick, loopEndTick;

	public static void init() {
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		setBPM(120);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setLoopBeginTick(0);
		setLoopEndTick(RESOLUTION * 4);
	}

	public static float getBPM() {
		return tempo.getBpm();
	}

	public static int setBPM(float bpm) {
		bpm = bpm >= MIN_BPM ? (bpm <= MAX_BPM ? bpm : MAX_BPM) : MIN_BPM;
		tempo.setBpm(bpm);
		setNativeBPM(bpm);
		setNativeMSPT(tempo.getMpqn() / RESOLUTION);
		TrackManager.quantizeEffectParams();
		return (int) bpm;
	}

	public static List<MidiNote> getMidiNotes() {
		List<MidiNote> midiNotes = new ArrayList<MidiNote>();
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				midiNotes.add(midiNote);
			}
		}
		return midiNotes;
	}

	public static List<MidiNote> getSelectedNotes() {
		ArrayList<MidiNote> selectedNotes = new ArrayList<MidiNote>();
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				if (midiNote.isSelected()) {
					selectedNotes.add(midiNote);
				}
			}
		}
		return selectedNotes;
	}

	public static void deselectAllNotes() {
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				deselectNote(midiNote);
			}
		}
	}

	// return true if any Midi note is selected
	public static boolean anyNoteSelected() {
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				if (midiNote.isSelected()) {
					return true;
				}
			}
		}
		return false;
	}

	public static void selectNote(MidiNote midiNote) {
		midiNote.setSelected(true);

		GlobalVars.mainPage.midiView.updateNoteFillColor(midiNote);
		updateEditIcons();
	}

	public static void deselectNote(MidiNote midiNote) {
		midiNote.setSelected(false);
		GlobalVars.mainPage.midiView.updateNoteFillColor(midiNote);
		updateEditIcons();
	}

	public static void selectRegion(long leftTick, long rightTick, int topNote,
			int bottomNote) {
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote midiNote : track.getMidiNotes()) {
				// conditions for region selection
				boolean a = leftTick < midiNote.getOffTick();
				boolean b = rightTick > midiNote.getOffTick();
				boolean c = leftTick < midiNote.getOnTick();
				boolean d = rightTick > midiNote.getOnTick();
				if (i >= topNote && i <= bottomNote
						&& ((a && b) || (c && d) || (!b && !c))) {
					selectNote(midiNote);
				} else {
					deselectNote(midiNote);
				}
			}
		}
	}

	public static void selectRow(int rowNum) {
		deselectAllNotes();
		for (MidiNote midiNote : TrackManager.getTrack(rowNum).getMidiNotes()) {
			selectNote(midiNote);
		}
	}

	public static MidiNote addNote(long onTick, long offTick, int note,
			float velocity, float pan, float pitch) {
		NoteOn on = new NoteOn(onTick, 0, note, velocity, pan, pitch);
		NoteOff off = new NoteOff(offTick, 0, note, velocity, pan, pitch);
		return addNote(on, off);
	}

	private static MidiNote addNote(NoteOn on, NoteOff off) {
		MidiNote midiNote = new MidiNote(on, off);
		addNote(midiNote);
		return midiNote;
	}

	private static void addNote(MidiNote midiNote) {
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.addNote(midiNote);
		GlobalVars.mainPage.midiView.createNoteView(midiNote);
	}

	public static void deleteNote(MidiNote midiNote) {
		midiNote.getRectangle().getGroup().remove(midiNote.getRectangle());
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.removeNote(midiNote);
		updateEditIcons();
	}

	private static void updateEditIcons() {
		boolean anyNoteSelected = anyNoteSelected();
		GlobalVars.mainPage.controlButtonGroup
				.setEditIconsEnabled(anyNoteSelected);
	}

	public static void copy() {
		if (!anyNoteSelected())
			return;
		copiedNotes = copyMidiList(getSelectedNotes());
	}

	public static boolean isCopying() {
		return !copiedNotes.isEmpty();
	}

	public static void cancelCopy() {
		copiedNotes.clear();
	}

	public static void paste(long startTick) {
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

	public static void deleteSelectedNotes() {
		if (anyNoteSelected())
			saveState();
		for (MidiNote selected : getSelectedNotes()) {
			deleteNote(selected);
		}
	}

	public static void clearNotes() {
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			while (!track.getMidiNotes().isEmpty()) {
				deleteNote(track.getMidiNotes().get(0)); // avoid concurrent mod
															// error
			}
		}
	}

	public static boolean setNoteValue(MidiNote midiNote, int newNote) {
		int oldNote = midiNote.getNoteValue();
		if (oldNote == newNote)
			return false;
		midiNote.setNote(newNote);
		Track oldTrack = TrackManager.getTrack(oldNote);
		Track newTrack = TrackManager.getTrack(newNote);
		oldTrack.removeNote(midiNote);
		newTrack.addNote(midiNote);
		return true;
	}

	public static boolean setNoteTicks(MidiNote midiNote, long onTick,
			long offTick, boolean snapToGrid, boolean maintainNoteLength) {
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
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		return track.setNoteTicks(midiNote, onTick, offTick);
	}

	public static Tempo getTempo() {
		return tempo;
	}

	public static long getTicksPerBeat(float beatDivision) {
		return (long) (RESOLUTION / beatDivision);
	}

	public static long millisToTick(long millis) {
		return (long) ((RESOLUTION * 1000f / tempo.getMpqn()) * millis);
	}

	public static long getLeftMostTick(List<MidiNote> notes) {
		long leftMostTick = Long.MAX_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() < leftMostTick)
				leftMostTick = midiNote.getOnTick();
		}
		return leftMostTick;
	}

	public static long getRightMostTick(List<MidiNote> notes) {
		long rightMostTick = Long.MIN_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOffTick() > rightMostTick)
				rightMostTick = midiNote.getOffTick();
		}
		return rightMostTick;
	}

	public static long getLeftMostSelectedTick() {
		return getLeftMostTick(getSelectedNotes());
	}

	public static long getRightMostSelectedTick() {
		return getRightMostTick(getSelectedNotes());
	}

	/*
	 * Translate the provided midi note to its on-tick's nearest major tick
	 * given the provided beat division
	 */
	public static void quantize(MidiNote midiNote, float beatDivision) {
		long diff = (long) TickWindowHelper.getMajorTickNearestTo(midiNote
				.getOnTick()) - midiNote.getOnTick();
		setNoteTicks(midiNote, midiNote.getOnTick() + diff,
				midiNote.getOffTick() + diff, false, true);
	}

	public static void saveNoteTicks() {
		for (MidiNote note : getMidiNotes()) {
			note.saveTicks();
		}
	}

	public static void handleMidiCollisions() {
		for (int trackNum = 0; trackNum < TrackManager.getNumTracks(); trackNum++) {
			TrackManager.getTrack(trackNum).handleNoteCollisions();
		}
	}

	// called after release of touch event - this
	// finalizes the note on/off ticks of all notes
	public static void finalizeNoteTicks() {
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
	public static void quantize(float beatDivision) {
		for (MidiNote midiNote : getMidiNotes()) {
			quantize(midiNote, beatDivision);
		}
	}

	public static void saveState() {
		undoStack.push(currState);
		currState = copyMidiList(getMidiNotes());
		// enforce max undo stack size
		if (undoStack.size() > GlobalVars.UNDO_STACK_SIZE)
			undoStack.remove(0);
	}

	public static void undo() {
		if (undoStack.isEmpty())
			return;
		List<MidiNote> lastState = undoStack.pop();
		clearNotes();
		for (MidiNote midiNote : lastState) {
			addNote(midiNote);
		}
		currState = copyMidiList(getMidiNotes());
	}

	private static List<MidiNote> copyMidiList(List<MidiNote> midiList) {
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

	public static void writeToFile(File outFile) {
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

	public static void importFromFile(FileInputStream in) {
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

	public static void setLoopBeginTick(long loopBeginTick) {
		if (loopBeginTick >= loopEndTick)
			return;
		MidiManager.loopBeginTick = loopBeginTick;
		setLoopBeginTickNative(loopBeginTick);
	}

	public static void setLoopEndTick(long loopEndTick) {
		if (loopEndTick <= loopBeginTick)
			return;
		MidiManager.loopEndTick = loopEndTick;
		setLoopEndTickNative(loopEndTick);
	}

	public static void setLoopTicks(long loopBeginTick, long loopEndTick) {
		MidiManager.loopBeginTick = loopBeginTick;
		MidiManager.loopEndTick = loopEndTick;
		setLoopTicksNative(loopBeginTick, loopEndTick);
	}

	public static long getLoopBeginTick() {
		return loopBeginTick;
	}

	public static long getLoopEndTick() {
		return loopEndTick;
	}

	public static native void isTrackPlaying(int trackNum);

	public static native void setNativeMSPT(long MSPT);

	public static native void setNativeBPM(float BPM);

	public static native void setCurrTick(long currTick);

	public static native long getCurrTick();

	public static native void setLoopBeginTickNative(long loopBeginTick);

	public static native void setLoopEndTickNative(long loopEndTick);

	public static native void setLoopTicksNative(long loopBeginTick,
			long loopEndTick);
}
