package com.kh.beatbot.event.track;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;

public class TrackMuteEvent extends Executable {
	private int trackId;

	public TrackMuteEvent(Track track) {
		this.trackId = track.getId();
	}

	@Override
	public void undo() {
		doExecute();
	}

	@Override
	public boolean doExecute() {
		Track track = TrackManager.getTrackById(trackId);
		track.mute(!track.isMuted());
		return true;
	}
}
