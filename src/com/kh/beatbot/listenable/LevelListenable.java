package com.kh.beatbot.listenable;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.SurfaceViewBase;

public abstract class LevelListenable extends SurfaceViewBase {
	public static final float[] BG_COLOR = new float[] { 0.3275f, 0.3994f,
			0.4465f, 1 };
	protected ArrayList<LevelListener> levelListeners = new ArrayList<LevelListener>();
	protected float level = .5f;
	protected float[] levelColor = Colors.VOLUME_COLOR.clone();
	protected float[] selectColor = { levelColor[0], levelColor[1],
			levelColor[2], .5f };

	protected boolean selected = false;

	public LevelListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void addLevelListener(LevelListener levelListener) {
		levelListeners.add(levelListener);
	}

	public void removeAllListeners() {
		levelListeners.clear();
	}

	@Override
	protected void init() {
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
		if (isEnabled()) // only notify listeners if enabled
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
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// no multitouch for this seekbar
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// no multitouch for this seekbar
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyPressed(this, false);
		selected = false;
	}
}
