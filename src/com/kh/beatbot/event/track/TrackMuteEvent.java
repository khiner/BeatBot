package com.kh.beatbot.event.track;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;

public class TrackMuteEvent implements Executable, Stateful {
	private int trackId;

	public TrackMuteEvent(Track track) {
		this.trackId = track.getId();
	}

	@Override
	public void undo() {
		doExecute();
	}

	@Override
	public void apply() {
		doExecute();
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
	}

	@Override
	public void doExecute() {
		Track track = TrackManager.getTrackById(trackId);
		track.mute(!track.isMuted());
	}
}
