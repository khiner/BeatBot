package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.event.midinotes.MidiNotesEventManager;
import com.kh.beatbot.event.track.TrackCreateEvent;
import com.kh.beatbot.file.MidiFile;
import com.kh.beatbot.listener.LoopWindowListener;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.listener.SnapToGridListener;
import com.kh.beatbot.listener.TempoListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;
import com.kh.beatbot.midi.event.MidiEvent;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.event.meta.Tempo;
import com.kh.beatbot.midi.event.meta.TimeSignature;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.track.Track;

public class MidiManager implements MidiNoteListener {
	public static final int MIN_BPM = 45, MAX_BPM = 300,
			TICKS_PER_NOTE = MidiFile.DEFAULT_RESOLUTION, UNDO_STACK_SIZE = 40,
			NOTES_PER_MEASURE = 4, TICKS_PER_MEASURE = TICKS_PER_NOTE * NOTES_PER_MEASURE,
			MIN_TICKS = TICKS_PER_NOTE / 2, MAX_TICKS = TICKS_PER_MEASURE * 4;

	public static final double LOG_2 = Math.log(2);

	private static boolean snapToGrid = true;
	private static int beatDivision = 0;
	private static long loopBeginTick, loopEndTick;

	private static TimeSignature ts = new TimeSignature();
	private static Tempo tempo = new Tempo();
	private static MidiTrack tempoTrack = new MidiTrack();
	private static List<MidiNote> copiedNotes = new ArrayList<MidiNote>(),
			currState = new ArrayList<MidiNote>();

	private static List<MidiNoteListener> midiNoteListeners = new ArrayList<MidiNoteListener>();
	private static List<LoopWindowListener> loopChangeListeners = new ArrayList<LoopWindowListener>();
	private static TempoListener tempoListener;
	private static SnapToGridListener snapToGridListener;
	private static MidiManager instance = new MidiManager();

	public static MidiManager get() {
		return instance;
	}

	public static void init() {
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(tempo);
		setBPM(getBPM()); // set native bpm & trigger event notification
	}

	public static void addMidiNoteListener(MidiNoteListener listener) {
		midiNoteListeners.add(listener);
	}

	public static void addLoopChangeListener(LoopWindowListener listener) {
		loopChangeListeners.add(listener);
	}

	public static void setTempoListener(TempoListener listener) {
		tempoListener = listener;
	}

	public static void setSnapToGridListener(SnapToGridListener listener) {
		snapToGridListener = listener;
	}

	public static boolean isSnapToGrid() {
		return snapToGrid;
	}

	public static void setSnapToGrid(boolean snapToGrid) {
		MidiManager.snapToGrid = snapToGrid;
		snapToGridListener.onSnapToGridChanged(snapToGrid);
	}

	public static MidiTrack getTempoTrack() {
		return tempoTrack;
	}

	public static float getBPM() {
		return tempo.getBpm();
	}

	public static void setBPM(float bpm) {
		bpm = GeneralUtils.clipTo(bpm, MIN_BPM, MAX_BPM);
		tempo.setBpm(bpm);
		setNativeMSPT(tempo.getMpqn() / TICKS_PER_NOTE);
		TrackManager.quantizeEffectParams();

		tempoListener.onTempoChange(bpm);
	}

	public static MidiNote addNote(long onTick, long offTick, int note) {
		return MidiNotesEventManager.createNote(note, onTick, offTick);
	}

	public static MidiNote findNote(int noteValue, long onTick) {
		final Track track = TrackManager.getTrackByNoteValue(noteValue);
		return null == track ? null : track.findNoteStarting(onTick);
	}

	public static MidiNote findNoteContaining(int noteValue, long tick) {
		final Track track = TrackManager.getTrackByNoteValue(noteValue);
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
		long tickOffset = startTick - TrackManager.getSelectedNoteTickWindow()[0];
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
		beatDivision = (int) (Math.log(MAX_TICKS / numTicksDisplayed) / LOG_2);
	}

	public static long getTicksPerBeat() {
		return TICKS_PER_NOTE / (1 << beatDivision);
	}

	public static long getMajorTickNearestTo(long tick) {
		long spacing = getMajorTickSpacing();
		long remainder = tick % spacing;
		return remainder == 0 ? tick : (remainder > spacing / 2 ? getMajorTickAfter(tick) : getMajorTickBefore(tick));
	}

	public static long getMajorTickBefore(long tick) {
		return tick - tick % getMajorTickSpacing();
	}

	public static long getMajorTickAfter(long tick) {
		return getMajorTickBefore(tick) + getMajorTickSpacing();
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

	public static void setLoopTicks(long loopBeginTick, long loopEndTick) {
		boolean changed = false;
		long quantizedBeginTick = (long) getMajorTickNearestTo(loopBeginTick);
		long quantizedEndTick = (long) getMajorTickNearestTo(loopEndTick);
		if (quantizedBeginTick != MidiManager.loopBeginTick && quantizedBeginTick < quantizedEndTick) {
			MidiManager.loopBeginTick = quantizedBeginTick;
			changed = true;
		}
		
		if (quantizedEndTick != MidiManager.loopEndTick && quantizedEndTick > quantizedBeginTick) {
			MidiManager.loopEndTick = quantizedEndTick;
			changed = true;
		}

		if (changed)
			notifyLoopWindowChanged();
	}

	// set new loop begin point, preserving loop length
	public static void translateLoopWindowTo(long tick) {
		long loopLength = getLoopLength();
		long newBeginTick = GeneralUtils.clipTo(tick, 0, MAX_TICKS - loopLength);
		MidiManager.setLoopTicks(newBeginTick, newBeginTick + loopLength);
	}

	public static void pinchLoopWindow(long beginTickDiff, long endTickDiff) {
		setLoopTicks(getLoopBeginTick() + beginTickDiff, getLoopEndTick() + endTickDiff);
	}

	public static long getLoopBeginTick() {
		return loopBeginTick;
	}

	public static long getLoopEndTick() {
		return loopEndTick;
	}

	public static long getLoopLength() {
		return getLoopEndTick() - getLoopBeginTick();
	}

	private static long getMajorTickSpacing() {
		return TICKS_PER_MEASURE / (2 << beatDivision);
	}

	@Override
	public void onCreate(MidiNote note) {
		for (MidiNoteListener listener : midiNoteListeners) {
			listener.onCreate(note);
		}
	}

	@Override
	public void onDestroy(MidiNote note) {
		for (MidiNoteListener listener : midiNoteListeners) {
			listener.onDestroy(note);
		}
	}

	@Override
	public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		for (MidiNoteListener listener : midiNoteListeners) {
			listener.onMove(note, beginNoteValue, beginOnTick, beginOffTick, endNoteValue,
					endOnTick, endOffTick);
		}
	}

	@Override
	public void onSelectStateChange(MidiNote note) {
		for (MidiNoteListener listener : midiNoteListeners) {
			listener.onSelectStateChange(note);
		}
	}

	@Override
	public void onLevelChanged(MidiNote note, LevelType type) {
		for (MidiNoteListener listener : midiNoteListeners) {
			listener.onLevelChanged(note, type);
		}
	}

	private static void notifyLoopWindowChanged() {
		setLoopTicksNative(loopBeginTick, loopEndTick);
		TrackManager.updateAllTrackNextNotes();
		for (LoopWindowListener listener : loopChangeListeners) {
			listener.onLoopWindowChange(loopBeginTick, loopEndTick);
		}
	}

	public static native void isTrackPlaying(int trackId);

	public static native void setNativeMSPT(long MSPT);

	public static native void setCurrTick(long currTick);

	public static native long getCurrTick();

	public static native void setLoopTicksNative(long loopBeginTick, long loopEndTick);
}
