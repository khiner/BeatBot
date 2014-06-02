package com.kh.beatbot.ui.transition;

public abstract class Transition {
	public enum State {
		OFF, UP, COMPLETE, DOWN
	}

	protected State currState = State.OFF;
	protected float rate = 0, position = 0; // between 0 & 1
	protected long waitFrames = 0, waitCount = 0;

	protected boolean released = false;

	public Transition(long durationInFrames) {
		this(durationInFrames, 0);
	}

	public Transition(long durationInFrames, long waitFrames) {
		rate = 1f / durationInFrames;
		this.waitFrames = waitFrames;
	}

	public State getState() {
		return currState;
	}

	public synchronized void tick() {
		switch (currState) {
		case OFF:
			return;
		case COMPLETE:
			if (released && waitCount++ >= waitFrames) {
				currState = State.DOWN;
			}
			return;
		case UP:
			position += rate;
			break;
		case DOWN:
			position -= rate;
			break;
		}
		if (position > 1) {
			position = 1;
			currState = State.COMPLETE;
		} else if (position < 0) {
			position = 0;
			currState = State.OFF;
		}
		update();
	}

	public void begin() {
		released = false;
		if (currState != State.COMPLETE)
			currState = State.UP;
	}

	public void end() {
		if (!released) {
			waitCount = 0;
			released = true;
		}
	}

	protected abstract void update();
}
