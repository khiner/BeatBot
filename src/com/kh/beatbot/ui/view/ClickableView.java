package com.kh.beatbot.ui.view;

import com.kh.beatbot.ui.shape.ShapeGroup;

public abstract class ClickableView extends LongPressableView {

	public ClickableView() {
		this(null);
	}

	public ClickableView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;

	/** State Variables for Clicking/Pressing **/
	private long lastDownTime = 0, lastTapTime = 0;
	

	/****************** Clickable Methods ********************/
	protected abstract void singleTap(int id, float x, float y);

	protected abstract void doubleTap(int id, float x, float y);

	@Override
	public void releaseLongPress() {
		super.releaseLongPress();
		lastDownTime = Long.MAX_VALUE;
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		lastDownTime = System.currentTimeMillis();
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.releaseLongPress();
		long time = System.currentTimeMillis();
		if (Math.abs(time - lastDownTime) < SINGLE_TAP_TIME) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - lastTapTime < DOUBLE_TAP_TIME
					&& Math.abs(x - lastTapX) <= SNAP_DIST
					&& Math.abs(y - lastTapY) <= SNAP_DIST) {
				doubleTap(id, x, y);
				// reset tap time so that a third tap doesn't register as
				// another double tap
				lastTapTime = 0;
			} else {
				lastTapX = x;
				lastTapY = y;
				lastTapTime = time;
				singleTap(id, x, y);
			}
		}
	}
}
