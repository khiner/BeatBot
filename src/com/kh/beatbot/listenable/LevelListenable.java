package com.kh.beatbot.listenable;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public abstract class LevelListenable extends SurfaceViewBase {
	protected ArrayList<LevelListener> levelListeners = new ArrayList<LevelListener>();
	
	protected float level = .5f;
	protected float[] levelColor = MidiViewBean.VOLUME_COLOR;
	
	public LevelListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void addLevelListener(LevelListener levelListener) {		
		levelListeners.add(levelListener);
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

	public void setViewLevel(float x) {
		this.level = x;
	}

	/* level should be from 0 to 1 */
	public void setLevel(float level) {
		setViewLevel(level);
		for (LevelListener levelListener : levelListeners)
			levelListener.setLevel(this, level);
	}
	
	public void setLevelColor(float[] levelColor) {
		this.levelColor = levelColor;
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyChecked(this, true);
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
			levelListener.notifyChecked(this, false);
	}
}
