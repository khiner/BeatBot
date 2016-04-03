package com.kh.beatbot.event.track;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;

public class TrackToggleLoopEvent extends Executable {
	private int trackId;

	public TrackToggleLoopEvent(Track track) {
		this.trackId = track.getId();
	}

	@Override
	public void undo() {
		doExecute();
	}

	@Override
	public boolean doExecute() {
		final Track track = View.context.getTrackManager().getTrackById(trackId);
		track.toggleLooping();
		return true;
	}
}
