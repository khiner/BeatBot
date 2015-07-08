package com.kh.beatbot.event;

import com.kh.beatbot.file.ProjectFile;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;

public class TrackCreateEvent implements Executable, Stateful {
	private int trackId;
	private String serializedTrack = null;

	public TrackCreateEvent() {
	}

	public TrackCreateEvent(Track track) {
		this.trackId = track.getId();
		this.serializedTrack = ProjectFile.toJson(track);
	}

	@Override
	public void undo() {
		TrackDestroyEvent trackDestroyEvent = new TrackDestroyEvent(trackId);
		trackDestroyEvent.doExecute();
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
		if (serializedTrack == null) {
			Track track = TrackManager.createTrack();
			trackId = track.getId();
		} else {
			ProjectFile.fromJson(serializedTrack);
		}
	}
}
