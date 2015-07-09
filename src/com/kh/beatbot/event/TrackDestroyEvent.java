package com.kh.beatbot.event;

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
		doExecute();
		EventManager.eventCompleted(this);
	}

	public void doExecute() {
		if (TrackManager.getNumTracks() > 1) {
			TrackManager.getTrackById(trackId).destroy();
		}
	}
}
