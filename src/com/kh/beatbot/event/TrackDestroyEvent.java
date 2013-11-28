package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;

public class TrackDestroyEvent implements Executable, Stateful {

	private Track track;

	public TrackDestroyEvent(Track track) {
		this.track = track;
	}

	@Override
	public void doUndo() {
		TrackCreateEvent createEvent = new TrackCreateEvent(track);
		createEvent.doExecute();
		createEvent.updateUi();
	}

	@Override
	public void doRedo() {
		doExecute();
		updateUi();
	}

	@Override
	public void updateUi() {
		View.mainPage.notifyTrackDeleted(track);
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
		updateUi();
	}

	public void doExecute() {
		TrackManager.setTrack(TrackManager.getTrack(track.getId()));
		TrackManager.deleteCurrTrack();
	}
}
