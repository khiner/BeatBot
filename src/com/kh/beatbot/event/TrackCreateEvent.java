package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;

public class TrackCreateEvent implements Executable, Stateful {

	private File sampleFile;
	private Track createdTrack = null;

	public TrackCreateEvent(Track track) {
		this.createdTrack = track;
	}

	public TrackCreateEvent(File sample) {
		this.sampleFile = sample;
	}

	@Override
	public void doUndo() {
		if (createdTrack != null) {
			TrackDestroyEvent trackDestroyEvent = new TrackDestroyEvent(createdTrack);
			trackDestroyEvent.doExecute(); 
			trackDestroyEvent.updateUi();
		}
	}

	@Override
	public void doRedo() {
		doExecute();
		updateUi();
	}

	@Override
	public void updateUi() {
		if (!TrackManager.trackExists(createdTrack)) {
			return;
		}
		// needed to avoid "no current context" opengl error
		View.root.queueEvent(new Runnable() {
			@Override
			public void run() {
				View.mainPage.notifyTrackCreated(createdTrack);
				TrackManager.setTrack(createdTrack);
			}
		});
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
		updateUi();
	}

	public void doExecute() {
		if (createdTrack == null) {
			createdTrack = TrackManager.createTrack(sampleFile);
		} else {
			TrackManager.createTrack(createdTrack);
		}
	}
}
