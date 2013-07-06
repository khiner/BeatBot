package com.kh.beatbot.ui.view.control;

import java.util.ArrayList;

import com.kh.beatbot.listener.Level1dListener;

public abstract class ControlView1dBase extends ControlViewBase {

	protected ArrayList<Level1dListener> levelListeners = new ArrayList<Level1dListener>();
	
	protected float level = .5f;
	
	protected abstract float posToLevel(float x, float y);
	
	public float getLevel() {
		return level;
	}

	public void setViewLevel(float level) {
		this.level = level;
	}
	
	public void addLevelListener(Level1dListener levelListener) {
		levelListeners.add(levelListener);
	}

	public void clearListeners() {
		levelListeners.clear();
	}
	
	/* level should be from 0 to 1 */
	public void setLevel(float level) {
		setViewLevel(level);
		for (Level1dListener levelListener : levelListeners) {
			levelListener.onLevelChange(this, level);
		}
	}
	
	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		setLevel(posToLevel(x, y));
	}
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (!selected)
			return;
		setLevel(posToLevel(x, y));
	}
}
