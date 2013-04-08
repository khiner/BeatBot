package com.kh.beatbot.view.control;

import java.util.ArrayList;

import com.kh.beatbot.listener.Level2dListener;

public abstract class ControlView2dBase extends ControlViewBase {

	protected ArrayList<Level2dListener> levelListeners = new ArrayList<Level2dListener>();

	protected float xLevel = .5f, yLevel = .5f;

	protected abstract float xToLevel(float x);
	protected abstract float yToLevel(float y);
	
	public void addLevelListener(Level2dListener levelListener) {
		levelListeners.add(levelListener);
	}

	public void clearListeners() {
		levelListeners.clear();
	}
	
	public void setLevel(float xLevel, float yLevel) {
		setViewLevel(xLevel, yLevel);
		for (Level2dListener listener : levelListeners) {
			listener.onLevelChange(this, xLevel, yLevel);
		}
	}
	
	public void setViewLevelX(float x) {
		xLevel = x;
	}

	public void setViewLevelY(float y) {
		yLevel = y;
	}

	public void setViewLevel(float x, float y) {
		xLevel = x;
		yLevel = y;
	}
	
	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		setLevel(xToLevel(x), yToLevel(y));
	}
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		setLevel(xToLevel(x), yToLevel(y));
	}
}
