package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class TrackManager implements MidiTrackControlListener {

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
		MidiTrackControlHelper.addListener(this);
	}

	public Track getTrack(int trackNum) {
		return tracks.get(trackNum);
	}

	public BaseTrack getBaseTrack(int trackNum) {
		if (trackNum == MASTER_TRACK_ID)
			return masterTrack;
		return tracks.get(trackNum);
	}
	
	public int getNumTracks() {
		return tracks.size();
	}

	public void addTrack(Instrument instrument, int sampleNum) {
		addTrack(instrument.getSamplePath(sampleNum));
		Track newTrack = new Track(tracks.size(), instrument, sampleNum);
		tracks.add(newTrack);
		currTrack = tracks.get(tracks.size() - 1);
		if (GlobalVars.midiView != null)
			GlobalVars.midiView.updateTracks();
	}

	public void clearNotes() {
		for (Track track : tracks) {
			track.clearNotes();
		}
	}

	@Override
	public void muteToggled(int track, boolean mute) {
		tracks.get(track).mute(mute);
	}

	@Override
	public void soloToggled(int track, boolean solo) {
		tracks.get(track).solo(solo);
	}

	@Override
	public void trackLongPressed(int track) {
		Managers.midiManager.selectRow(track);
	}

	@Override
	public void trackClicked(int track) {
		Track newTrack = tracks.get(track);
		if (newTrack == currTrack)
			return;
		currTrack = newTrack;
		PageManager.updateTrackPages();
	}

	public static native void addTrack(String sampleFileName);

	/******* These methods are called FROM native code via JNI ********/

	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public void updateAllTrackNextNotes() {
		for (Track track : tracks) {
			track.updateNextNote();
		}
	}
}
