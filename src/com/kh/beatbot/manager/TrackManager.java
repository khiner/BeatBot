package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.layout.page.TrackPageFactory;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class TrackManager implements MidiTrackControlListener {

	private static TrackManager singletonInstance = null;
	
	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	private static List<Track> tracks = new ArrayList<Track>();
	
	public static TrackManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new TrackManager();
		}
		return singletonInstance;
	}
	
	private TrackManager() {
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			Track track = new Track(tracks.size(), Managers.directoryManager.getDrumInstrument(i));
			tracks.add(track);
			addTrack(track.getSamplePath());
		}
		MidiTrackControlHelper.addListener(this);
	}
	
	public Track getTrack(int trackNum) {
		return tracks.get(trackNum);
	}

	public int getNumTracks() {
		return tracks.size();
	}
	
	public void addTrack(int instrumentType) {
		Instrument newInstrument = Managers.directoryManager.getDrumInstrument(instrumentType);
		addTrack(newInstrument);
	}
	
	public void addTrack(Instrument instrument) {
		Track newTrack = new Track(tracks.size(), instrument); 
		addTrack(newTrack.getSamplePath());
		tracks.add(newTrack);
		GlobalVars.midiView.updateTracks();
		trackClicked(tracks.size() - 1);
	}
	
	public void clearNotes() {
		for (Track track : tracks) {
			track.notes.clear();
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
		TrackPageFactory.setTrack(tracks.get(track));
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
