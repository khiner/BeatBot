package com.kh.beatbot.view;

import com.kh.beatbot.global.GlobalVars;

public abstract class ClickableBBView extends TouchableBBView {

	public ClickableBBView(TouchableSurfaceView parent) {
		super(parent);
	}

	/** State Variables for Clicking/Pressing **/
	private long lastDownTime = 0;
	private long lastTapTime = 0;
	private float lastTapX = -1;
	private float lastTapY = -1;


	/****************** Clickable Methods ********************/
	protected abstract void singleTap(int id, float x, float y);

	protected abstract void doubleTap(int id, float x, float y);

	protected abstract void longPress(int id, float x, float y);

	@Override
	protected void handleActionDown(int id, float x, float y) {
		lastDownTime = System.currentTimeMillis();
		lastTapX = x;
		lastTapY = y;
	}

	@Override
	protected void handleActionPointerDown(int id, float x, float y) {
		// nothing to do
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		if (Math.abs(x - lastTapX) < 25
				&& Math.abs(y - lastTapY) < 25) {
			if (System.currentTimeMillis() - lastDownTime > GlobalVars.LONG_CLICK_TIME) {
				longPress(id, x, y);
			}
		} else {
			lastDownTime = Long.MAX_VALUE;
		}
	}

	@Override
	protected void handleActionPointerUp(int id, float x, float y) {
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
