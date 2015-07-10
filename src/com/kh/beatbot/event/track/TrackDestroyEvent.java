package com.kh.beatbot.event.track;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.file.ProjectFile;
import com.kh.beatbot.manager.TrackManager;

public class TrackDestroyEvent implements Executable, Stateful {
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

	@Override
	public void apply() {
		doExecute();
	}

	@Override
	public void execute() {
		if (allowed()) {
			doExecute();
			EventManager.eventCompleted(this);
		}
	}

	public void doExecute() {
		if (allowed()) {
			TrackManager.getTrackById(trackId).destroy();
		}
	}
	
	private boolean allowed() {
		return TrackManager.getNumTracks() > 1;
	}
}
