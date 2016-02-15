package com.kh.beatbot.event;

import com.kh.beatbot.ui.view.View;

public class TempoChangeEvent extends Executable implements Temporal {
	private float beginBpm, endBpm;

	public TempoChangeEvent() {
	}

	public TempoChangeEvent(float newBpm) {
		begin();
		this.endBpm = newBpm;
	}

	public void setEndBpm(float bpm) {
		endBpm = bpm;
	}

	@Override
	public void undo() {
		new TempoChangeEvent(beginBpm).apply();
	}

	@Override
	public void begin() {
		beginBpm = View.context.getMidiManager().getBpm();
	}

	@Override
	public void end() {
		endBpm = View.context.getMidiManager().getBpm();
		execute();
	}

	@Override
	public boolean doExecute() {
		if (endBpm != beginBpm) {
			View.context.getMidiManager().setBpm(endBpm);
			return true;
		} else {
			return false;
		}
	}

}
