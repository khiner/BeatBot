package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.event.midinotes.MidiNotesEventManager;
import com.kh.beatbot.file.MidiFile;
import com.kh.beatbot.listener.LoopWindowListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.track.Track;

public class MidiManager {
	public static final int MIN_BPM = 45, MAX_BPM = 300,
			TICKS_PER_NOTE = MidiFile.DEFAULT_RESOLUTION, UNDO_STACK_SIZE = 40,
			NOTES_PER_MEASURE = 4, TICKS_PER_MEASURE = TICKS_PER_NOTE * NOTES_PER_MEASURE,
			MIN_TICKS = TICKS_PER_NOTE / 2, MAX_TICKS = TICKS_PER_MEASURE * 4;

	private static boolean snapToGrid = true;
	private static int beatDivision = 0;
	private static long loopBeginTick, loopEndTick;

	private static TimeSignature ts = new TimeSignature();
	private static Tempo tempo = new Tempo();
	private static MidiTrack tempoTrack = new MidiTrack();
	private static List<MidiNote> copiedNotes = new ArrayList<MidiNote>(),
			currState = new ArrayList<MidiNote>();

	private static List<LoopWindowListener> loopChangeListeners = new ArrayList<LoopWindowListener>();

	public static void init() {
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
	}

	public static void addLoopChangeListener(LoopWindowListener listener) {
		loopChangeListeners.add(listener);
	}

	public static boolean isSnapToGrid() {
		return snapToGrid;
	}

	public static void setSnapToGrid(boolean snapToGrid) {
		MidiManager.snapToGrid = snapToGrid;
	}

	public static MidiTrack getTempoTrack() {
		return tempoTrack;
	}

	public static float getBPM() {
		return tempo.getBpm();
	}

	public static int setBPM(float bpm) {
		bpm = GeneralUtils.clipTo(bpm, MIN_BPM, MAX_BPM);
		tempo.setBpm(bpm);
		setNativeBPM(bpm);
		setNativeMSPT(tempo.getMpqn() / TICKS_PER_NOTE);
		TrackManager.quantizeEffectParams();
		return (int) bpm;
	}

	public static MidiNote addNote(long onTick, long offTick, int note) {
		return MidiNotesEventManager.createNote(note, onTick, offTick);
	}

	public static MidiNote findNote(int noteValue, long onTick) {
		final Track track = TrackManager.getTrack(noteValue);
		return null == track ? null : track.findNoteStarting(onTick);
	}

	public static MidiNote findNoteContaining(int noteValue, long tick) {
		final Track track = TrackManager.getTrack(noteValue);
		return null == track ? null : track.findNoteContaining(tick);
	}

	public static void deleteNote(MidiNote midiNote) {
		MidiNotesEventManager.destroyNote(midiNote);
	}

	public static void copy() {
		if (TrackManager.anyNoteSelected()) {
			copiedNotes = TrackManager.copySelectedNotes();
		}
	}

	public static boolean isCopying() {
		return !copiedNotes.isEmpty();
	}

	public static void cancelCopy() {
		copiedNotes.clear();
	}

	public static void paste(long startTick) {
		if (copiedNotes.isEmpty())
			return;
		// Copied notes should still be selected, so leftmostSelectedTick should be accurate
		long tickOffset = startTick - TrackManager.getSelectedTickWindow()[0];
		for (MidiNote copiedNote : copiedNotes) {
			copiedNote.setTicks(copiedNote.getOnTick() + tickOffset, copiedNote.getOffTick()
					+ tickOffset);
		}

		MidiNotesEventManager.createNotes(copiedNotes);
		copiedNotes.clear();
	}

	public static int getBeatDivision() {
		return beatDivision;
	}

	public static void adjustBeatDivision(float numTicksDisplayed) {
		beatDivision = (int) (Math.log(MAX_TICKS / numTicksDisplayed) / Math.log(2));
	}

	public static long getTicksPerBeat() {
		return TICKS_PER_NOTE / (1 << beatDivision);
	}

	public static long millisToTick(long millis) {
		return (long) ((TICKS_PER_NOTE * 1000f / tempo.getMpqn()) * millis);
	}

	public static long getMajorTickSpacing() {
		return TICKS_PER_MEASURE / (2 << beatDivision);
	}

	public static float getMajorTickNearestTo(float tick) {
		float spacing = getMajorTickSpacing();
		if (tick % spacing > spacing / 2) {
			return tick + spacing - tick % spacing;
		} else {
			return tick - tick % spacing;
		}
	}

	public static float getMajorTickToLeftOf(float tick) {
		return tick - tick % getMajorTickSpacing();
	}

	public static void quantize() {
		MidiNotesEventManager.quantize(beatDivision);
	}

	public static void importFromFile(MidiFile midiFile) {
		List<MidiNote> newNotes = new ArrayList<MidiNote>();
		ArrayList<MidiTrack> midiTracks = midiFile.getTracks();
		tempoTrack = midiTracks.get(0);
		ts = (TimeSignature) tempoTrack.getEvents().get(0);
		tempo = (Tempo) tempoTrack.getEvents().get(1);
		setNativeMSPT(tempo.getMpqn() / TICKS_PER_NOTE);
		ArrayList<MidiEvent> events = midiTracks.get(1).getEvents();

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
						// TODO store all midi notes and then add them
						// together in one event
						MidiNote note = new MidiNote(on, off);
						newNotes.add(note);
						unfinishedNotes.remove(j);
						break;
					}
				}
			}
		}

		// create any necessary tracks
		for (MidiNote note : newNotes) {
			while (note.getNoteValue() >= TrackManager.getNumTracks()) {
				new TrackCreateEvent().execute();
			}
		}

		MidiNotesEventManager.begin();
		MidiNotesEventManager.destroyAllNotes();
		MidiNotesEventManager.createNotes(newNotes);
		MidiNotesEventManager.end();
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
		for (LoopWindowListener listener : loopChangeListeners) {
			listener.onLoopWindowChange(loopBeginTick, loopEndTick);
		}
	}

	public static long getLoopBeginTick() {
		return loopBeginTick;
	}

	public static long getLoopEndTick() {
		return loopEndTick;
	}

	public static native void isTrackPlaying(int trackId);

	public static native void setNativeMSPT(long MSPT);

	public static native void setNativeBPM(float BPM);

	public static native void setCurrTick(long currTick);

	public static native long getCurrTick();

	public static native void setLoopTicksNative(long loopBeginTick, long loopEndTick);
}
