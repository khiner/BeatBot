package com.kh.beatbot.event.track;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
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
		this.serializedTrack = ProjectFile.trackToJson(track);
	}

	@Override
	public void undo() {
		new TrackDestroyEvent(trackId).apply();
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
		if (serializedTrack == null) {
			Track track = TrackManager.createTrack();
			trackId = track.getId();
		} else {
			ProjectFile.trackFromJson(serializedTrack);
		}
	}
}
