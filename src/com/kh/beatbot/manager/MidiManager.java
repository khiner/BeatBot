package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.event.LoopWindowSetEvent;
import com.kh.beatbot.event.midinotes.MidiNotesCreateEvent;
import com.kh.beatbot.event.midinotes.MidiNotesDestroyEvent;
import com.kh.beatbot.event.midinotes.MidiNotesGroupEvent;
import com.kh.beatbot.event.midinotes.MidiNotesLevelsSetEvent;
import com.kh.beatbot.event.midinotes.MidiNotesMoveEvent;
import com.kh.beatbot.event.midinotes.MidiNotesPinchEvent;
import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;
import com.kh.beatbot.ui.view.page.Page;

public class MidiManager {

	public static final int MIN_BPM = 45, MAX_BPM = 300,
	// ticks per quarter note (I think)
			RESOLUTION = MidiFile.DEFAULT_RESOLUTION, UNDO_STACK_SIZE = 40;

	public static final long TICKS_IN_ONE_MEASURE = RESOLUTION * 4;
	public static final float MIN_TICKS = TICKS_IN_ONE_MEASURE / 8;
	public static final float MAX_TICKS = TICKS_IN_ONE_MEASURE * 4;

	public static float currBeatDivision;

	private static boolean snapToGrid = true;

	private static TimeSignature ts = new TimeSignature();
	private static Tempo tempo = new Tempo();
	private static MidiTrack tempoTrack = new MidiTrack();

	private static List<MidiNote> copiedNotes = new ArrayList<MidiNote>(),
			currState = new ArrayList<MidiNote>();

	private static long loopBeginTick, loopEndTick;

	private static MidiNotesGroupEvent currNoteEvent;
	private static LoopWindowSetEvent currLoopWindowEvent;

	public static void init() {
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER,
				TimeSignature.DEFAULT_DIVISION);
		setBPM(120);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setLoopBeginTick(0);
		setLoopEndTick(RESOLUTION * 4);
	}

	public static boolean toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
		return snapToGrid;
	}

	public static float getBPM() {
		return tempo.getBpm();
	}

	public static int setBPM(float bpm) {
		bpm = GeneralUtils.clipTo(bpm, MIN_BPM, MAX_BPM);
		tempo.setBpm(bpm);
		setNativeBPM(bpm);
		setNativeMSPT(tempo.getMpqn() / RESOLUTION);
		TrackManager.quantizeEffectParams();
		return (int) bpm;
	}

	public static synchronized void beginMidiEvent(Track track) {
		endMidiEvent();
		currNoteEvent = track == null ? new MidiNotesGroupEvent() : new MidiNotesLevelsSetEvent(track);
		currNoteEvent.begin();
		currLoopWindowEvent = new LoopWindowSetEvent();
		currLoopWindowEvent.begin();
	}

	public static synchronized void endMidiEvent() {
		if (currNoteEvent != null) {
			currNoteEvent.end();
			currNoteEvent = null;
		}
		if (currLoopWindowEvent != null) {
			currLoopWindowEvent.end();
			currLoopWindowEvent = null;
		}
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

		Page.mainPage.midiView.updateNoteFillColor(midiNote);
		Page.mainPage.controlButtonGroup.setEditIconsEnabled(anyNoteSelected());
	}

	public static void deselectNote(MidiNote midiNote) {
		midiNote.setSelected(false);
		Page.mainPage.midiView.updateNoteFillColor(midiNote);
		Page.mainPage.controlButtonGroup.setEditIconsEnabled(anyNoteSelected());
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
		new MidiNotesCreateEvent(midiNote).execute();
	}

	public static void deleteNote(MidiNote midiNote) {
		new MidiNotesDestroyEvent(midiNote).execute();
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
		Page.mainPage.controlButtonGroup.uncheckCopyButton();
		if (copiedNotes.isEmpty())
			return;

		long tickOffset = startTick - getLeftmostTick(copiedNotes);
		for (MidiNote copiedNote : copiedNotes) {
			copiedNote.setOnTick(copiedNote.getOnTick() + tickOffset);
			copiedNote.setOffTick(copiedNote.getOffTick() + tickOffset);
		}

		beginMidiEvent(null);
		new MidiNotesCreateEvent(copyMidiList(copiedNotes)).execute();
		endMidiEvent();
		copiedNotes.clear();
	}

	public static void deleteSelectedNotes() {
		beginMidiEvent(null);
		new MidiNotesDestroyEvent(getSelectedNotes()).execute();
		endMidiEvent();
	}

	public static void setNoteValue(MidiNote midiNote, int newNote) {
		int oldNote = midiNote.getNoteValue();
		if (oldNote == newNote)
			return;
		midiNote.setNote(newNote);
		TrackManager.getTrack(oldNote).removeNote(midiNote);
		TrackManager.getTrack(newNote).addNote(midiNote);
	}

	public static void setNoteTicks(MidiNote midiNote, long onTick,
			long offTick, boolean maintainNoteLength) {
		if (midiNote.getOnTick() == onTick && midiNote.getOffTick() == offTick)
			return;
		if (offTick <= onTick)
			offTick = onTick + 4;
		if (snapToGrid) {
			onTick = (long) TickWindowHelper.getMajorTickNearestTo(onTick);
			offTick = (long) TickWindowHelper.getMajorTickNearestTo(offTick) - 1;
			if (offTick == onTick - 1) {
				offTick += getTicksPerBeat(currBeatDivision);
			}
		}
		if (maintainNoteLength)
			offTick = midiNote.getOffTick() + onTick - midiNote.getOnTick();
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.setNoteTicks(midiNote, onTick, offTick);
	}

	public static void moveNotes(List<MidiNote> notes, long tickDiff,
			int noteDiff) {
		new MidiNotesMoveEvent(notes, tickDiff, noteDiff).execute();
	}

	public static void pinchNotes(List<MidiNote> notes, long onTickDiff,
			long offTickDiff) {
		new MidiNotesPinchEvent(getSelectedNotes(), onTickDiff, offTickDiff)
				.execute();
	}

	public static long getTicksPerBeat(float beatDivision) {
		return (long) (RESOLUTION / beatDivision);
	}

	public static long millisToTick(long millis) {
		return (long) ((RESOLUTION * 1000f / tempo.getMpqn()) * millis);
	}

	public static long getLeftmostSelectedTick() {
		return getLeftmostTick(getSelectedNotes());
	}

	public static long getRightmostSelectedTick() {
		return getRightmostTick(getSelectedNotes());
	}

	/*
	 * Translate the provided midi note to its on-tick's nearest major tick
	 * given the provided beat division
	 */
	public static void quantize(MidiNote midiNote, float beatDivision) {
		long diff = (long) TickWindowHelper.getMajorTickNearestTo(midiNote
				.getOnTick()) - midiNote.getOnTick();
		setNoteTicks(midiNote, midiNote.getOnTick() + diff,
				midiNote.getOffTick() + diff, true);
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
			if (midiNote.getOnTick() > MAX_TICKS) {
				Log.e("MidiManager", "Deleting note in finalize!");
				deleteNote(midiNote);
			} else {
				midiNote.finalizeTicks();
			}
		}
	}

	public static void quantize() {
		quantize(currBeatDivision);
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

	public static List<MidiNote> copyMidiList(List<MidiNote> midiList) {
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
			new MidiNotesDestroyEvent(getMidiNotes()).execute();
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
							// TODO stoe all midi notes and then add them
							// together in one event
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
		setLoopTicks(loopBeginTick, loopEndTick);
	}

	public static void setLoopEndTick(long loopEndTick) {
		if (loopEndTick <= loopBeginTick)
			return;
		setLoopTicks(loopBeginTick, loopEndTick);
	}

	public static void setLoopTicks(long loopBeginTick, long loopEndTick) {
		MidiManager.loopBeginTick = loopBeginTick;
		MidiManager.loopEndTick = loopEndTick;
		setLoopTicksNative(loopBeginTick, loopEndTick);
		TrackManager.updateAllTrackNextNotes();
	}

	public static long getLoopBeginTick() {
		return loopBeginTick;
	}

	public static long getLoopEndTick() {
		return loopEndTick;
	}

	private static long getLeftmostTick(List<MidiNote> notes) {
		long leftMostTick = Long.MAX_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() < leftMostTick)
				leftMostTick = midiNote.getOnTick();
		}
		return leftMostTick;
	}

	private static long getRightmostTick(List<MidiNote> notes) {
		long rightMostTick = Long.MIN_VALUE;
		for (MidiNote midiNote : notes) {
			if (midiNote.getOffTick() > rightMostTick)
				rightMostTick = midiNote.getOffTick();
		}
		return rightMostTick;
	}

	public static native void isTrackPlaying(int trackNum);

	public static native void setNativeMSPT(long MSPT);

	public static native void setNativeBPM(float BPM);

	public static native void setCurrTick(long currTick);

	public static native long getCurrTick();

	public static native void setLoopTicksNative(long loopBeginTick,
			long loopEndTick);
}
