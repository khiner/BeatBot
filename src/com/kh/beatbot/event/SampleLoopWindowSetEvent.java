package com.kh.beatbot.event;

import com.kh.beatbot.track.Track;

public class SampleLoopWindowSetEvent implements Stateful, Temporal {
	private Track track;
	private float initialBeginLevel = 0, initialEndLevel = 0, finalBeginLevel = 0,
			finalEndLevel = 0;

	public SampleLoopWindowSetEvent(Track track) {
		this.track = track;
	}

	@Override
	public void begin() {
		initialBeginLevel = track.getLoopBeginParam().viewLevel;
		initialEndLevel = track.getLoopEndParam().viewLevel;
	}

	@Override
	public void end() {
		finalBeginLevel = track.getLoopBeginParam().viewLevel;
		finalEndLevel = track.getLoopEndParam().viewLevel;

		if (initialBeginLevel != finalBeginLevel || initialEndLevel != finalEndLevel) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		track.setSampleLoopWindow(initialBeginLevel, initialEndLevel);
	}

	@Override
	public void apply() {
		track.setSampleLoopWindow(finalBeginLevel, finalEndLevel);
	}
}
