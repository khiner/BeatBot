package com.kh.beatbot.event;

public interface Stateful {
	public abstract void doUndo();

	public abstract void doRedo();
}
