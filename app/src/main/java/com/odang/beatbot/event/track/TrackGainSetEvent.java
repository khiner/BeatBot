package com.odang.beatbot.event.track;

import com.odang.beatbot.event.Stateful;
import com.odang.beatbot.event.Temporal;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

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
