package com.kh.beatbot.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.TrackButtonRow;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackManager {

	public static final int MASTER_TRACK_ID = -1;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	private static List<Track> tracks = new ArrayList<Track>();
	public static BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	public static Track currTrack;

	public static synchronized void init() {
		for (File drumDirectory : DirectoryManager.drumsDirectory.listFiles()) {
			TrackCreateEvent trackCreateEvent = new TrackCreateEvent(drumDirectory.listFiles()[0]);
			trackCreateEvent.doExecute();
			trackCreateEvent.updateUi();
		}
	}

	public static synchronized Track getTrack(int trackNum) {
		return tracks.get(trackNum);
	}

	public static synchronized void quantizeEffectParams() {
		for (Track track : tracks) {
			track.quantizeEffectParams();
		}
	}

	public static synchronized void setTrack(Track track) {
		if (track == currTrack)
			return;
		currTrack = track;
		track.select();
		View.mainPage.notifyTrackChanged(currTrack);
	}

	public static synchronized BaseTrack getBaseTrack(int trackNum) {
		if (trackNum == MASTER_TRACK_ID)
			return masterTrack;
		return tracks.get(trackNum);
	}

	public static synchronized int getNumTracks() {
		return tracks.size();
	}

	public static synchronized Track createTrack(File sampleFile) {
		createTrack(sampleFile.getPath());
		final Track newTrack = new Track(tracks.size(), sampleFile);
		tracks.add(newTrack);
		return newTrack;
	}

	public static synchronized void createTrack(Track track) {
		createTrack(track.getCurrSampleFile().getPath());
		track.setId(tracks.size());
		tracks.add(track);
		track.updateADSR();
		track.updateNextNote();
	}

	public static synchronized void deleteCurrTrack() {
		if (tracks.size() <= 1) {
			return; // not allowed to delete last track
		}
		int currTrackNum = currTrack.getId();
		tracks.remove(currTrack);
		for (int i = currTrackNum; i < tracks.size(); i++) {
			tracks.get(i).setId(i);
		}
		setTrack(tracks.get(Math.min(currTrackNum, tracks.size() - 1)));
		deleteTrack(currTrackNum);
	}

	public static boolean trackExists(Track track) {
		return tracks.contains(track);
	}

	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public static synchronized void updateAllTrackNextNotes() {
		for (Track track : tracks) {
			track.updateNextNote();
		}
	}

	public static synchronized void selectInstrumentButton(ToggleButton button) {
		button.setChecked(true);
		for (Track track : tracks) {
			TrackButtonRow buttonRow = track.getButtonRow();
			if (!button.equals(buttonRow.instrumentButton)) {
				buttonRow.instrumentButton.setChecked(false);
			}
		}
	}

	public static synchronized void selectSoloButton(ToggleButton button) {
		button.setChecked(true);
		for (Track track : tracks) {
			TrackButtonRow buttonRow = track.getButtonRow();
			if (!button.equals(buttonRow.soloButton)) {
				buttonRow.soloButton.setChecked(false);
			}
		}
	}

	/******* These methods are called FROM native code via JNI ********/

	public static native void createTrack(String sampleFileName);

	public static native void deleteTrack(int trackNum);
}
