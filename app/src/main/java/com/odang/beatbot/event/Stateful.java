package com.odang.beatbot.event;

public interface Stateful {
	public abstract void undo();

	public abstract void apply();
}
