package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;

public class TrackDestroyEvent implements Executable, Stateful {

	private Track track;

	public TrackDestroyEvent(Track track) {
		this.track = track;
	}

	@Override
	public void undo() {
		TrackCreateEvent createEvent = new TrackCreateEvent(track);
		createEvent.doExecute();
	}

	@Override
	public void redo() {
		doExecute();
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
	}

	public void doExecute() {
		if (TrackManager.getNumTracks() > 1) {
			track.destroy(); // not allowed to delete last track
		}
	}
}
