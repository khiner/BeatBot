package com.kh.beatbot.event.track;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.file.ProjectFile;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;

public class TrackCreateEvent extends Executable {
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
	public boolean doExecute() {
		if (serializedTrack == null) {
			final Track track = View.context.getTrackManager().createTrack();
			trackId = track.getId();
		} else {
			ProjectFile.trackFromJson(serializedTrack);
		}
		return true;
	}
}
