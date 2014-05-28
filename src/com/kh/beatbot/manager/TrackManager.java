package com.kh.beatbot.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.listener.TrackLevelsEventListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.midi.MidiNote;

public class TrackManager implements TrackListener, FileListener, MidiNoteListener {
	public static final int MASTER_TRACK_ID = -1;
	public static Track currTrack, soloingTrack;

	private static final TrackManager instance = new TrackManager();
	private static final BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	private static final List<Track> tracks = Collections.synchronizedList(new ArrayList<Track>());
	private static final Set<TrackListener> trackListeners = new HashSet<TrackListener>();
	private static final Set<TrackLevelsEventListener> trackLevelsEventListeners = new HashSet<TrackLevelsEventListener>();

	private TrackManager() {
		FileManager.addListener(this);
	}

	public static TrackManager get() {
		return instance;
	}

	public static BaseTrack getMasterTrack() {
		return masterTrack;
	}

	public static List<Track> getTracks() {
		return tracks; // XXX shouldn't provide full list - concurrency threat
	}

	public static void addTrackListener(TrackListener trackListener) {
		trackListeners.add(trackListener);
	}

	public static void addTrackLevelsEventListener(TrackLevelsEventListener listener) {
		trackLevelsEventListeners.add(listener);
	}

	public static void notifyTrackLevelsSetEvent(BaseTrack track) {
		track.select();
		for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
			listener.onTrackLevelsChange(track);
		}
	}

	public static void notifyNoteLevelsSetEvent(Track track) {
		track.select();
		for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
			listener.onNoteLevelsChange(track);
		}
	}

	public static void notifyLoopWindowSetEvent(Track track) {
		track.select();
		for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
			listener.onSampleLoopWindowChange(track);
		}
	}

	public static void init() {
		for (File drumDirectory : FileManager.drumsDirectory.listFiles()) {
			new TrackCreateEvent().doExecute();
			tracks.get(tracks.size() - 1).setSample(drumDirectory.listFiles()[0]);
		}
	}

	public static void destroy() {
		List<Track> tracksToDestroy = new ArrayList<Track>();
		tracksToDestroy.addAll(tracks);
		for (Track track : tracksToDestroy) {
			track.destroy();
		}
	}

	public static void selectRow(int rowNum) {
		deselectAllNotes();
		getTrack(rowNum).selectAllNotes();
	}

	public static void deselectAllNotes() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.deselectAllNotes();
			}
		}
	}

	public static List<MidiNote> getMidiNotes() {
		List<MidiNote> midiNotes = new ArrayList<MidiNote>();
		synchronized (tracks) {
			for (Track track : tracks) {
				midiNotes.addAll(track.getMidiNotes());
			}
		}
		return midiNotes;
	}

	public static MidiNote getMidiNote(int trackNum, long tick) {
		Track track = getTrack(trackNum);
		return null == track ? null : track.getMidiNote(tick);
	}

	public static boolean notesEqual(List<MidiNote> otherNotes) {
		List<MidiNote> notes = getMidiNotes();
		if (notes.size() != otherNotes.size())
			return false;
		for (int i = 0; i < notes.size(); i++) {
			if (notes.get(i).compareTo(otherNotes.get(i)) != 0) {
				return false;
			}
		}
		return true;
	}

	public static List<MidiNote> copySelectedNotes() {
		List<MidiNote> selectedNotesCopy = new ArrayList<MidiNote>();
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected()) {
						selectedNotesCopy.add(note.getCopy());
					}
				}
			}
		}
		return selectedNotesCopy;
	}

	public static List<MidiNote> copyMidiNotes() {
		List<MidiNote> midiNotesCopy = new ArrayList<MidiNote>();
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					midiNotesCopy.add(note.getCopy());
				}
			}
		}
		return midiNotesCopy;
	}

	public static List<MidiNote> getSelectedNotes() {
		ArrayList<MidiNote> selectedNotes = new ArrayList<MidiNote>();
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected()) {
						selectedNotes.add(note);
					}
				}
			}
		}
		return selectedNotes;
	}

	public static float getAdjustedTickDiff(float tickDiff, long startOnTick, MidiNote singleNote) {
		if (tickDiff == 0)
			return 0;
		float adjustedTickDiff = tickDiff;
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected()) {
						if (null != singleNote && !note.equals(singleNote))
							continue;
						if (Math.abs(startOnTick - note.getOnTick()) + Math.abs(tickDiff) <= 10) {
							// inside threshold distance - set to original position
							return startOnTick - note.getOnTick();
						}
						if (note.getOnTick() < -adjustedTickDiff) {
							adjustedTickDiff = -note.getOnTick();
						} else if (MidiManager.MAX_TICKS - note.getOffTick() < adjustedTickDiff) {
							adjustedTickDiff = MidiManager.MAX_TICKS - note.getOffTick();
						}
					}
				}
			}
		}
		return adjustedTickDiff;
	}

	public static int getAdjustedNoteDiff(int noteDiff, MidiNote singleNote) {
		int adjustedNoteDiff = noteDiff;
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected()) {
						if (singleNote != null && !note.equals(singleNote))
							continue;
						if (note.getNoteValue() < -adjustedNoteDiff) {
							adjustedNoteDiff = -note.getNoteValue();
						} else if (tracks.size() - 1 - note.getNoteValue() < adjustedNoteDiff) {
							adjustedNoteDiff = tracks.size() - 1 - note.getNoteValue();
						}
					}
				}
			}
		}
		return adjustedNoteDiff;
	}

	public static void moveNote(MidiNote note, int noteDiff, long tickDiff) {
		Track track = getTrack(note);
		if (null != track) {
			track.moveNote(note, noteDiff, tickDiff);
			track.removeNote(note);
			track = getTrack(note);
			if (null != track) {
				track.addNote(note);
			}
		}
	}

	public static void moveSelectedNotes(int noteDiff, long tickDiff) {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.moveSelectedNotes(noteDiff, tickDiff);
			}
			for (Track track : tracks) {
				track.resetSelectedNotes();
			}
		}
	}

	public static void setNoteTicks(MidiNote midiNote, long onTick, long offTick,
			boolean maintainNoteLength) {
		Track track = getTrack(midiNote);
		track.setNoteTicks(midiNote, onTick, offTick, maintainNoteLength);
	}

	public static void pinchSelectedNotes(long onTickDiff, long offTickDiff) {
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected()) {
						pinchNote(note, onTickDiff, offTickDiff);
					}
				}
			}
		}
	}

	public static void selectRegion(long leftTick, long rightTick, int topNote, int bottomNote) {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.selectRegion(leftTick, rightTick, topNote, bottomNote);
			}
		}
	}

	public static void saveNoteTicks() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.saveNoteTicks();
			}
		}
	}

	// called after release of touch event - this
	// finalizes the note on/off ticks of all notes
	public static void finalizeNoteTicks() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.finalizeNoteTicks();
			}
		}
	}

	// return true if any Midi note exists
	public static boolean anyNotes() {
		synchronized (tracks) {
			for (Track track : tracks) {
				if (track.anyNotes()) {
					return true;
				}
			}
			return false;
		}
	}

	// return true if any Midi note is selected
	public static boolean anyNoteSelected() {
		synchronized (tracks) {
			for (Track track : tracks) {
				if (track.anyNoteSelected()) {
					return true;
				}
			}
		}
		return false;
	}

	public static long[] getSelectedTickWindow() {
		long[] selectedTickWindow = { Long.MAX_VALUE, Long.MIN_VALUE };
		synchronized (tracks) {
			for (Track track : tracks) {
				for (MidiNote note : track.getMidiNotes()) {
					if (note.isSelected() && note.getOnTick() < selectedTickWindow[0]) {
						selectedTickWindow[0] = note.getOnTick();
					}
					if (note.isSelected() && note.getOffTick() > selectedTickWindow[1]) {
						selectedTickWindow[1] = note.getOffTick();
					}
				}
			}
		}
		return selectedTickWindow;
	}

	/*
	 * Translate all midi notes to their on-ticks' nearest major ticks given the provided beat
	 * division
	 */
	public static void quantize(int beatDivision) {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.quantize(beatDivision);
			}
		}
	}

	public static Track getTrack(int noteValue) {
		return noteValue < tracks.size() ? tracks.get(noteValue) : null;
	}

	public static Track getTrack(MidiNote note) {
		return getTrack(note.getNoteValue());
	}

	public static BaseTrack getBaseTrack(int trackNum) {
		return trackNum == MASTER_TRACK_ID ? masterTrack : tracks.get(trackNum);
	}

	public static Track getSoloingTrack() {
		return soloingTrack;
	}

	public static int getNumTracks() {
		return tracks.size();
	}

	public static Track createTrack() {
		createTrackNative();
		final Track newTrack;
		synchronized (tracks) {
			newTrack = new Track(tracks.size());
			tracks.add(newTrack);
		}
		get().onCreate(newTrack);
		return newTrack;
	}

	public static void createTrack(Track track) {
		createTrackNative();
		synchronized (tracks) {
			track.setId(tracks.size());
			tracks.add(track);
		}
		get().onCreate(track);

		track.setSample(track.getCurrSampleFile());
	}

	public static void quantizeEffectParams() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.quantizeEffectParams();
			}
		}
	}

	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public static void updateAllTrackNextNotes() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.updateNextNote();
			}
		}
	}

	/******* These methods are called FROM native code via JNI ********/

	public static native void createTrackNative();

	@Override
	public void onCreate(Track track) {
		currTrack = track;
		for (TrackListener trackListener : trackListeners) {
			trackListener.onCreate(track);
		}
		track.updateNextNote();
		track.select();
	}

	@Override
	public void onDestroy(Track track) {
		synchronized (tracks) {
			tracks.remove(track);
			for (int i = track.getId(); i < tracks.size(); i++) {
				tracks.get(i).setId(i);
			}
			if (!tracks.isEmpty()) {
				tracks.get(Math.min(track.getId(), tracks.size() - 1)).select();
			}
		}

		for (TrackListener trackListener : trackListeners) {
			trackListener.onDestroy(track);
		}
	}

	@Override
	public void onSelect(BaseTrack track) {
		if (track instanceof Track) {
			currTrack = (Track) track;
			currTrack.getButtonRow().instrumentButton.setChecked(true);
		} else {
			currTrack = null;
		}

		synchronized (tracks) {
			for (Track otherTrack : tracks) {
				if (!track.equals(otherTrack)) {
					otherTrack.getButtonRow().instrumentButton.setChecked(false);
				}
			}
		}
		for (TrackListener trackListener : trackListeners) {
			trackListener.onSelect(track);
		}
	}

	@Override
	public void onSampleChange(Track track) {
		for (TrackListener trackListener : trackListeners) {
			trackListener.onSampleChange(track);
		}
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
		for (TrackListener trackListener : trackListeners) {
			trackListener.onMuteChange(track, mute);
		}
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
		soloingTrack = solo ? track : null;
		if (solo) {
			synchronized (tracks) {
				// if this track is soloing, set all other solo icons to inactive.
				for (Track otherTrack : tracks) {
					if (!track.equals(otherTrack)) {
						otherTrack.getButtonRow().soloButton.setChecked(false);
					}
				}
			}
		}
		for (TrackListener trackListener : trackListeners) {
			trackListener.onSoloChange(track, solo);
		}
	}

	@Override
	public void onNameChange(File file, File newFile) {
		synchronized (tracks) {
			for (Track track : tracks) {
				if (track.getCurrSampleFile().equals(file)) {
					track.onNameChange(file, newFile);
				}
			}
		}
	}

	private static void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		float newOnTick = midiNote.getOnTick();
		float newOffTick = midiNote.getOffTick();
		if (midiNote.getOnTick() + onTickDiff >= 0)
			newOnTick += onTickDiff;
		if (midiNote.getOffTick() + offTickDiff <= MidiManager.MAX_TICKS)
			newOffTick += offTickDiff;
		setNoteTicks(midiNote, (long) newOnTick, (long) newOffTick, false);
	}

	@Override
	public void onCreate(MidiNote note) {
		Track track = getTrack(note);
		if (null != track) {
			track.addNote(note);
		}
	}

	@Override
	public void onDestroy(MidiNote note) {
		Track track = getTrack(note);
		if (null != track) {
			track.removeNote(note);
		}
	}

	@Override
	public void onMove(MidiNote note) {
		// no-op
	}

	@Override
	public void onSelectStateChange(MidiNote note) {
		// no-op
	}
}
