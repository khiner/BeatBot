package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;

public class TrackCreateEvent implements Executable, Stateful {

	private Track createdTrack = null;

	public TrackCreateEvent() {
	}

	public TrackCreateEvent(Track track) {
		this.createdTrack = track;
	}

	@Override
	public void undo() {
		if (createdTrack != null) {
			TrackDestroyEvent trackDestroyEvent = new TrackDestroyEvent(createdTrack);
			trackDestroyEvent.doExecute();
		}
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
		if (createdTrack == null) {
			createdTrack = TrackManager.createTrack();
		} else {
			TrackManager.createTrack(createdTrack);
		}
	}
}
