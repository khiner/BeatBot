package com.kh.beatbot.event.track;

import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;

public class TrackGainSetEvent implements Stateful, Temporal {
	private int trackId;
	private float initialLevel = 0, finalLevel = 0;

	public TrackGainSetEvent(int trackId) {
		this.trackId = trackId;
	}

	@Override
	public void begin() {
		final Track track = View.context.getTrackManager().getTrackById(trackId);
		initialLevel = track.getGainParam().viewLevel;
	}

	@Override
	public void end() {
		final Track track = View.context.getTrackManager().getTrackById(trackId);
		finalLevel = track.getGainParam().viewLevel;

		if (initialLevel != finalLevel) {
			View.context.getEventManager().eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		final Track track = View.context.getTrackManager().getTrackById(trackId);
		track.setSampleGain(initialLevel);
	}

	@Override
	public void apply() {
		final Track track = View.context.getTrackManager().getTrackById(trackId);
		track.setSampleGain(finalLevel);
	}
}
