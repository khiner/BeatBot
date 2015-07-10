package com.kh.beatbot.event;

public abstract class Executable implements Stateful {
	public void execute() {
		if (doExecute()) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void apply() {
		doExecute();
	}

	public abstract boolean doExecute();
}
