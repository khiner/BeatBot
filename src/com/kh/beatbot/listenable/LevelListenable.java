package com.kh.beatbot.listenable;

import java.util.ArrayList;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;

public abstract class LevelListenable extends TouchableViewWindow {

	protected ArrayList<LevelListener> levelListeners = new ArrayList<LevelListener>();
	protected float level = .5f;
	protected float[] levelColor = Colors.VOLUME.clone();
	protected float[] selectColor = { levelColor[0], levelColor[1],
			levelColor[2], .5f };

	protected boolean selected = false;

	public LevelListenable(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void addLevelListener(LevelListener levelListener) {
		levelListeners.add(levelListener);
	}

	public void removeAllListeners() {
		levelListeners.clear();
	}

	@Override
	public void init() {
		for (LevelListener levelListener : levelListeners) {
			levelListener.notifyInit(this);
		}
	}

	public float getLevel() {
		return level;
	}

	public void setViewLevel(float level) {
		this.level = level;
	}

	/* level should be from 0 to 1 */
	public void setLevel(float level) {
		setViewLevel(level);
		for (LevelListener levelListener : levelListeners)
			levelListener.setLevel(this, level);
	}

	public void setLevelColor(float[] newLevelColor) {
		levelColor = newLevelColor;
		selectColor = new float[] { levelColor[0], levelColor[1],
				levelColor[2], .5f };
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyPressed(this, true);
		selected = true;
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyPressed(this, false);
		selected = false;
		requestRender();
	}
}
