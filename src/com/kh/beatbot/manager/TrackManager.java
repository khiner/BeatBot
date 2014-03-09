package com.kh.beatbot.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.midi.MidiNote;

public class TrackManager implements TrackListener {

	private static TrackManager instance;

	private TrackManager() {
	}

	public synchronized static TrackManager get() {
		if (instance == null) {
			instance = new TrackManager();
		}
		return instance;
	}

	public static final int MASTER_TRACK_ID = -1;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	private static List<Track> tracks = new ArrayList<Track>();
	public static BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	public static Track currTrack, soloingTrack;

	private static Set<TrackListener> trackListeners = new HashSet<TrackListener>();

	public static void addTrackListener(TrackListener trackListener) {
		trackListeners.add(trackListener);
	}

	public static synchronized void init() {
		for (File drumDirectory : FileManager.drumsDirectory.listFiles()) {
			final TrackCreateEvent trackCreateEvent = new TrackCreateEvent();
			trackCreateEvent.doExecute();
			trackCreateEvent.updateUi();
			tracks.get(tracks.size() - 1).setSample(drumDirectory.listFiles()[0]);
		}
	}

	public static synchronized Track getTrack(int trackNum) {
		return tracks.get(trackNum);
	}

	public static synchronized BaseTrack getBaseTrack(int trackNum) {
		if (trackNum == MASTER_TRACK_ID)
			return masterTrack;
		return tracks.get(trackNum);
	}

	public static Track getSoloingTrack() {
		return soloingTrack;
	}

	public static synchronized int getNumTracks() {
		return tracks.size();
	}

	public static List<Track> getTracks() {
		return tracks;
	}

	public static synchronized Track createTrack() {
		createTrackNative();
		final Track newTrack = new Track(tracks.size());
		tracks.add(newTrack);
		get().onCreate(newTrack);
		return newTrack;
	}

	public static synchronized void createTrack(Track track) {
		createTrackNative();
		track.setId(tracks.size());
		tracks.add(track);
		track.setSample(track.getCurrSampleFile());
		track.updateNextNote();
		get().onCreate(track);
	}

	public static boolean trackExists(Track track) {
		return tracks.contains(track);
	}

	public static synchronized void quantizeEffectParams() {
		for (Track track : tracks) {
			track.quantizeEffectParams();
		}
	}

	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public static synchronized void updateAllTrackNextNotes() {
		for (Track track : tracks) {
			track.updateNextNote();
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
		track.select();
	}

	@Override
	public void onDestroy(Track track) {
		tracks.remove(track);
		for (int i = track.getId(); i < tracks.size(); i++) {
			tracks.get(i).setId(i);
		}
		tracks.get(Math.min(track.getId(), tracks.size() - 1)).select();

		for (TrackListener trackListener : trackListeners) {
			trackListener.onDestroy(track);
		}
	}

	@Override
	public void onSelect(BaseTrack track) {
		if (track instanceof Track)
			currTrack = (Track) track;
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
		for (TrackListener trackListener : trackListeners) {
			trackListener.onSoloChange(track, solo);
		}
	}
}
