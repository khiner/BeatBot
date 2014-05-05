package com.kh.beatbot.ui.view;

import com.kh.beatbot.ui.shape.RenderGroup;

public abstract class ClickableView extends LongPressableView {

	public ClickableView() {
		this(null);
	}

	public ClickableView(RenderGroup renderGroup) {
		super(renderGroup);
	}

	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;

	/** State Variables for Clicking/Pressing **/
	private long lastDownTime = 0, lastTapTime = 0;

	/****************** Clickable Methods ********************/
	protected abstract void singleTap(int id, Pointer pos);

	protected abstract void doubleTap(int id, Pointer pos);

	@Override
	public synchronized void releaseLongPress() {
		super.releaseLongPress();
		lastDownTime = Long.MAX_VALUE;
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		lastDownTime = System.currentTimeMillis();
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.releaseLongPress();
		long time = System.currentTimeMillis();
		if (Math.abs(time - lastDownTime) < SINGLE_TAP_TIME) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - lastTapTime < DOUBLE_TAP_TIME && Math.abs(pos.x - lastTapX) <= SNAP_DIST
					&& Math.abs(pos.y - lastTapY) <= SNAP_DIST) {
				doubleTap(id, pos);
				// reset tap time so that a third tap doesn't register as
				// another double tap
				lastTapTime = 0;
			} else {
				lastTapX = pos.x;
				lastTapY = pos.y;
				lastTapTime = time;
				singleTap(id, pos);
			}
		}
		release();
	}
}
