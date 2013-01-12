package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.GlobalVars;

public abstract class ClickableSurfaceView extends TouchableSurfaceView {
	/** State Variables for Clicking/Pressing **/
	private long lastDownTime = 0;
	private long lastTapTime = 0;
	private float lastTapX = -1;
	private float lastTapY = -1;

	public ClickableSurfaceView(Context c, AttributeSet as) {
		super(c, as);
	}

	/****************** Clickable Methods ********************/
	protected abstract void singleTap(int id, float x, float y);

	protected abstract void doubleTap(int id, float x, float y);

	protected abstract void longPress(int id, float x, float y);

	@Override
	protected void init() {
		// nothing to do
	}

	@Override
	protected void drawFrame() {
		// nothing to do
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		lastDownTime = System.currentTimeMillis();
		lastTapX = x;
		lastTapY = y;
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// nothing to do
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		if (Math.abs(e.getX() - lastTapX) < 25
				&& Math.abs(e.getY() - lastTapY) < 25) {
			if (System.currentTimeMillis() - lastDownTime > GlobalVars.LONG_CLICK_TIME) {
				longPress(0, e.getX(), e.getY());
			}
		} else {
			lastDownTime = Long.MAX_VALUE;
		}
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// nothing to do

	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		long time = System.currentTimeMillis();
		if (Math.abs(time - lastDownTime) < GlobalVars.SINGLE_TAP_TIME) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - lastTapTime < GlobalVars.DOUBLE_TAP_TIME
					&& Math.abs(x - lastTapX) <= 25
					&& Math.abs(y - lastTapY) <= 25) {
				doubleTap(id, x, y);
				// reset tap time so that a third tap doesn't register as
				// another double tap
				lastTapTime = 0;
			} else {
				lastTapX = x;
				lastTapY = y;
				lastTapTime = System.currentTimeMillis();
				singleTap(id, x, y);
			}
		}
		lastDownTime = Long.MAX_VALUE;
	}

}
