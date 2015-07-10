package com.kh.beatbot.event.track;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.file.ProjectFile;
import com.kh.beatbot.manager.TrackManager;

public class TrackDestroyEvent extends Executable {
	private int trackId;
	private String serializedTrack = null;

	public TrackDestroyEvent(int trackId) {
		this.trackId = trackId;
		this.serializedTrack = ProjectFile.trackToJson(TrackManager.getTrackById(trackId));
	}

	@Override
	public void undo() {
		ProjectFile.trackFromJson(serializedTrack);
	}

	public boolean doExecute() {
		if (TrackManager.getNumTracks() > 1) {
			TrackManager.getTrackById(trackId).destroy();
			return true;
		} else {
			return false;
		}
	}
}
