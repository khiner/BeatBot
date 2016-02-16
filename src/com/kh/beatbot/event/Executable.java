package com.kh.beatbot.event;

import com.kh.beatbot.ui.view.View;

public abstract class Executable implements Stateful {
	public void execute() {
		if (doExecute()) {
			View.context.getEventManager().eventCompleted(this);
		}
	}

	@Override
	public void apply() {
		doExecute();
	}

	public abstract boolean doExecute();
}
