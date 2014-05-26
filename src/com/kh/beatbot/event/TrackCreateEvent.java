package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;

public class TrackCreateEvent implements Executable, Stateful {

	private Track createdTrack = null;

	public TrackCreateEvent(Track track) {
		this.createdTrack = track;
	}

	public TrackCreateEvent() {
	}

	@Override
	public void doUndo() {
		if (createdTrack != null) {
			TrackDestroyEvent trackDestroyEvent = new TrackDestroyEvent(createdTrack);
			trackDestroyEvent.doExecute();
		}
	}

	@Override
	public void doRedo() {
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
