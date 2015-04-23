package com.kh.beatbot.event;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;

public class SampleLoopWindowSetEvent implements Stateful, Temporal {
	private int trackId;
	private float initialBeginLevel = 0, initialEndLevel = 0, finalBeginLevel = 0,
			finalEndLevel = 0;

	public SampleLoopWindowSetEvent(int trackId) {
		this.trackId = trackId;
	}

	@Override
	public void begin() {
		Track track = TrackManager.getTrack(trackId);
		initialBeginLevel = track.getLoopBeginParam().viewLevel;
		initialEndLevel = track.getLoopEndParam().viewLevel;
	}

	@Override
	public void end() {
		Track track = TrackManager.getTrack(trackId);
		finalBeginLevel = track.getLoopBeginParam().viewLevel;
		finalEndLevel = track.getLoopEndParam().viewLevel;

		if (initialBeginLevel != finalBeginLevel || initialEndLevel != finalEndLevel) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		Track track = TrackManager.getTrack(trackId);
		track.setSampleLoopWindow(initialBeginLevel, initialEndLevel);
	}

	@Override
	public void apply() {
		Track track = TrackManager.getTrack(trackId);
		track.setSampleLoopWindow(finalBeginLevel, finalEndLevel);
	}
}
