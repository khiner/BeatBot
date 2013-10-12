package com.kh.beatbot.event;

public abstract class StateEvent extends Event {
	protected abstract void doUndo();
	protected abstract void doRedo();
	protected abstract void updateUi();
}
