package com.kh.beatbot.event;

import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;

public class SampleLoopWindowSetEvent implements Stateful, Temporal {
	private int trackId;
	private float initialBeginLevel = 0, initialEndLevel = 0, finalBeginLevel = 0,
			finalEndLevel = 0;

	public SampleLoopWindowSetEvent(int trackId) {
		this.trackId = trackId;
	}

	@Override
	public void begin() {
		Track track = View.context.getTrackManager().getTrackById(trackId);
		initialBeginLevel = track.getLoopBeginParam().viewLevel;
		initialEndLevel = track.getLoopEndParam().viewLevel;
	}

	@Override
	public void end() {
		Track track = View.context.getTrackManager().getTrackById(trackId);
		finalBeginLevel = track.getLoopBeginParam().viewLevel;
		finalEndLevel = track.getLoopEndParam().viewLevel;

		if (initialBeginLevel != finalBeginLevel || initialEndLevel != finalEndLevel) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		Track track = View.context.getTrackManager().getTrackById(trackId);
		track.setSampleLoopWindow(initialBeginLevel, initialEndLevel);
	}

	@Override
	public void apply() {
		Track track = View.context.getTrackManager().getTrackById(trackId);
		track.setSampleLoopWindow(finalBeginLevel, finalEndLevel);
	}
}
