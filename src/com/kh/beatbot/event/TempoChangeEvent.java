package com.kh.beatbot.event;

import com.kh.beatbot.manager.MidiManager;

public class TempoChangeEvent extends Executable implements Temporal {
	private float beginBpm, endBpm;

	public TempoChangeEvent() {
	}

	public TempoChangeEvent(float newBpm) {
		begin();
		this.endBpm = newBpm;
	}

	@Override
	public void undo() {
		new TempoChangeEvent(beginBpm).apply();
	}

	@Override
	public void begin() {
		beginBpm = MidiManager.getBPM();
	}

	@Override
	public void end() {
		endBpm = MidiManager.getBPM();
		execute();
	}

	@Override
	public boolean doExecute() {
		if (endBpm != beginBpm) {
			MidiManager.setBPM(endBpm);
			return true;
		} else {
			return false;
		}
	}

}
