package com.kh.beatbot.manager;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.SampleFile;
import com.kh.beatbot.Track;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.TrackButtonRow;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.Page;

public class TrackManager {

	public static final int MASTER_TRACK_ID = -1;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	private static List<Track> tracks = new ArrayList<Track>();
	public static BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
	public static Track currTrack;

	public static void init() {
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			createTrack(DirectoryManager.getDrumInstrument(i).getSample(0));
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

	public static void setTrack(Track track) {
		if (track == currTrack)
			return;
		currTrack = track;
		track.select();
		Page.mainPage.notifyTrackChanged(currTrack);
	}

	public static BaseTrack getBaseTrack(int trackNum) {
		if (trackNum == MASTER_TRACK_ID)
			return masterTrack;
		return tracks.get(trackNum);
	}

	public static int getNumTracks() {
		return tracks.size();
	}

	public static void createTrack(SampleFile sample) {
		createTrack(sample.getFullPath());
		final Track newTrack = new Track(tracks.size());
		newTrack.setSample(sample);
		tracks.add(newTrack);
		
		// needed to avoid "no current context" opengl error
		View.root.queueEvent(new Runnable() {
			@Override
			public void run() {
				Page.mainPage.notifyTrackCreated(newTrack);
				setTrack(newTrack);
			}
		});
	}

	public static void deleteCurrTrack() {
		if (tracks.size() <= 1) {
			return; // not allowed to delete last track
		}
		int currTrackNum = tracks.indexOf(currTrack);
		tracks.remove(currTrackNum);
		for (int i = currTrackNum; i < tracks.size(); i++) {
			tracks.get(i).setId(i);
		}
		Page.mainPage.notifyTrackDeleted(currTrack);
		setTrack(tracks.get(Math.min(currTrackNum, tracks.size() - 1)));
		deleteTrack(currTrackNum);
	}

	public static MidiNote getNextMidiNote(int trackNum, long currTick) {
		return tracks.get(trackNum).getNextMidiNote(currTick);
	}

	public static void updateAllTrackNextNotes() {
		for (Track track : tracks) {
			track.updateNextNote();
		}
	}

	public static void selectInstrumentButton(ToggleButton button) {
		button.setChecked(true);
		for (Track track : tracks) {
			TrackButtonRow buttonRow = track.getButtonRow();
			if (!button.equals(buttonRow.instrumentButton)) {
				buttonRow.instrumentButton.setChecked(false);
			}
		}
	}

	public static void selectSoloButton(ToggleButton button) {
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
