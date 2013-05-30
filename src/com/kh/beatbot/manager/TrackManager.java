package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.midi.MidiNote;

public class TrackManager {

	public static final int MASTER_TRACK_ID = -1;
	private static TrackManager singletonInstance = null;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	private static List<Track> tracks = new ArrayList<Track>();

	public static BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	
	public static Track currTrack;
	
	public static TrackManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new TrackManager();
		}
		return singletonInstance;
	}

	private TrackManager() {
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			addTrack(Managers.directoryManager.getDrumInstrument(i), 0);
		}
	}

	public static Track getTrack(int trackNum) {
		return tracks.get(trackNum);
	}

	public static void quantizeEffectParams() {
		for (Track track : tracks) {
			track.quantizeEffectParams();
		}
	}
	
	public static void setTrack(int trackNum) {
		Track newTrack = tracks.get(trackNum);
		if (newTrack == currTrack)
			return;
		currTrack = newTrack;
		GlobalVars.mainPage.midiView.notifyTrackChanged(trackNum);
		GlobalVars.mainPage.pageSelectGroup.notifyTrackChanged();
	}
	
	public static BaseTrack getBaseTrack(int trackNum) {
		if (trackNum == MASTER_TRACK_ID)
			return masterTrack;
		return tracks.get(trackNum);
	}
	
	public static int getNumTracks() {
		return tracks.size();
	}

	public static void addTrack(Instrument instrument, int sampleNum) {
		addTrack(instrument.getFullPath(sampleNum));
		Track newTrack = new Track(tracks.size());
		newTrack.setInstrument(instrument, sampleNum);
		tracks.add(newTrack);
		currTrack = tracks.get(tracks.size() - 1);
		GlobalVars.mainPage.trackAdded(tracks.size() - 1);
	}

	public static void deleteCurrTrack() {
		// TODO
	}
	
	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public static void updateAllTrackNextNotes() {
		for (Track track : tracks) {
			track.updateNextNote();
		}
	}
	
	/******* These methods are called FROM native code via JNI ********/
	
	public static native void addTrack(String sampleFileName);

}
