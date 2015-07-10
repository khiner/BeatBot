package com.kh.beatbot.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.listener.TrackLevelsEventListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;

public class TrackManager implements TrackListener, FileListener, MidiNoteListener {
	public static final int MASTER_TRACK_ID = -1;

	private static final TrackManager instance = new TrackManager();
	private static final BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	private static final List<Track> tracks = Collections.synchronizedList(new ArrayList<Track>());
	private static final Set<TrackListener> trackListeners = new HashSet<TrackListener>();
	private static final Set<TrackLevelsEventListener> trackLevelsEventListeners = new HashSet<TrackLevelsEventListener>();

	private static AlertDialog.Builder sampleSaveErrorAlert;

	private static Track currTrack;

	private TrackManager() {
		FileManager.addListener(this);
		MidiManager.addMidiNoteListener(this);
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
		if (!track.equals(currTrack)) {
			track.select();
		}
		for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
			listener.onTrackLevelsChange(track);
		}
	}

	public static void notifyLoopWindowSetEvent(Track track) {
		if (!track.equals(currTrack)) {
			track.select();
		}
		for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
			listener.onSampleLoopWindowChange(track);
		}
	}

	public static void init(Context context) {
		sampleSaveErrorAlert = new AlertDialog.Builder(context);
		sampleSaveErrorAlert.setPositiveButton("OK", null);
	}

	public static void setSample(Track track, File sampleFile) {
		try {
			track.setSample(sampleFile);
		} catch (Exception e) {
			sampleSaveErrorAlert.setTitle("Error loading " + sampleFile.getName() + ".")
					.setMessage(e.getMessage()).create().show();
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
		getTrackByNoteValue(rowNum).selectAllNotes();
	}

	public static void deselectAllNotes() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.deselectAllNotes();
			}
		}
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

	public static Track getTrackByNoteValue(int noteValue) {
		return noteValue < tracks.size() ? tracks.get(noteValue) : null;
	}

	public static BaseTrack getCurrTrack() {
		return currTrack == null ? masterTrack : currTrack;
	}

	public static Track getTrackById(int trackId) {
		for (Track track : tracks) {
			if (track.getId() == trackId)
				return track;
		}

		return null;
	}

	public static Track getTrack(MidiNote note) {
		return getTrackByNoteValue(note.getNoteValue());
	}

	public static BaseTrack getBaseTrackById(int trackId) {
		return trackId == MASTER_TRACK_ID ? masterTrack : getTrackById(trackId);
	}

	public static Track getSoloingTrack() {
		for (Track track : tracks) {
			if (track.isSoloing())
				return track;
		}
		return null;
	}

	public static int getNumTracks() {
		return tracks.size();
	}

	public static Track createTrack() {
		int id = tracks.isEmpty() ? 0 : tracks.get(tracks.size() - 1).getId() + 1;
		return createTrack(id, tracks.size());
	}

	public static Track createTrack(int trackId, int position) {
		createTrackNative(trackId);
		final Track newTrack;
		synchronized (tracks) {
			newTrack = new Track(trackId);
			tracks.add(position, newTrack);
		}
		get().onCreate(newTrack);
		return newTrack;
	}

	public static void quantizeEffectParams() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.quantizeEffectParams();
			}
		}
	}

	public static MidiNote getNextMidiNote(int trackId, long currTick) {
		return getTrackById(trackId).getNextMidiNote(currTick);
	}

	public static void updateAllTrackNextNotes() {
		synchronized (tracks) {
			for (Track track : tracks) {
				track.updateNextNote();
			}
		}
	}

	/******* These methods are called FROM native code via JNI ********/

	public static native void createTrackNative(int trackId);

	@Override
	public void onCreate(Track track) {
		for (int i = 0; i < tracks.size(); i++) {
			tracks.get(i).setNoteValue(i);
		}
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
			for (int i = 0; i < tracks.size(); i++) {
				tracks.get(i).setNoteValue(i);
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
		if (currTrack == track)
			return; // already selected

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
	public void onEffectCreate(BaseTrack track, Effect effect) {
		track.select();
		for (TrackListener trackListener : trackListeners) {
			trackListener.onEffectCreate(track, effect);
		}
	}

	@Override
	public void onEffectDestroy(BaseTrack track, Effect effect) {
		track.select();
		for (TrackListener trackListener : trackListeners) {
			trackListener.onEffectDestroy(track, effect);
		}
	}

	@Override
	public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
			int endEffectPosition) {
		track.select();
		for (TrackListener trackListener : trackListeners) {
			trackListener.onEffectOrderChange(track, initialEffectPosition, endEffectPosition);
		}
	}

	@Override
	public void onSampleChange(Track track) {
		track.select();
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
		if (solo) {
			for (Track otherTrack : tracks) {
				if (!otherTrack.equals(track) && otherTrack.isSoloing()) {
					otherTrack.solo(false);
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
	public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		if (beginNoteValue == endNoteValue) {
			Track track = getTrackByNoteValue(beginNoteValue);
			// if we're changing the stop tick on a note that's already playing to a
			// note before the current tick, or moving the start tick to after the playhead
			// stop the track
			long currTick = MidiManager.getCurrTick();
			boolean playing = beginOnTick <= currTick && beginOffTick >= currTick;
			if (playing && endOffTick < currTick || endOnTick > currTick) {
				track.stop();
			} else {
				track.updateNextNote();
			}
		} else {
			Track oldTrack = getTrackByNoteValue(beginNoteValue);
			Track newTrack = getTrackByNoteValue(endNoteValue);
			if (null != oldTrack)
				oldTrack.removeNote(note);
			if (null != newTrack)
				newTrack.addNote(note);
		}
	}

	@Override
	public void onSelectStateChange(MidiNote note) {
		// no-op
	}

	@Override
	public void onLevelChanged(MidiNote note, LevelType type) {
		// no-op
	}
}
