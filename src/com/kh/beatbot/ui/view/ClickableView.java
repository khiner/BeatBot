package com.kh.beatbot.ui.view;

import android.os.Handler;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class ClickableView extends TouchableView {

	Runnable longPressed = new Runnable() {
		public void run() {
			if (pointerIdToPos.isEmpty())
				return;
			int id = pointerIdToPos.keySet().iterator().next();
			Position pos = pointerIdToPos.get(id);
			longPress(id, pos.x, pos.y);
			longPressing = false;
		}
	};

	public ClickableView() {
		this(null);

	}

	public ClickableView(ShapeGroup shapeGroup) {
		super(shapeGroup);
		BeatBotActivity.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				handler = new Handler();
			}
		});
	}

	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	// time (in millis) for a long press in one location
	public final static long LONG_PRESS_TIME = 600;

	// if pointers go out of this radius, they will cancel taps, double taps and
	// long presses
	public final static float SNAP_DIST = 30;

	// handler for timed events
	private Handler handler;

	/** State Variables for Clicking/Pressing **/
	private long lastDownTime = 0, lastTapTime = 0;
	private float lastTapX = -1, lastTapY = -1;
	private boolean longPressing = false;

	/****************** Clickable Methods ********************/
	protected abstract void singleTap(int id, float x, float y);

	protected abstract void doubleTap(int id, float x, float y);

	protected abstract void longPress(int id, float x, float y);

	private void beginLongPress() {
		handler.postDelayed(longPressed, LONG_PRESS_TIME);
		longPressing = true;
	}

	protected void releaseLongPress() {
		if (null != handler) {
			handler.removeCallbacks(longPressed);
		}
		longPressing = false;
	}
	
	protected boolean isLongPressing() {
		return longPressing;
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		beginLongPress();
		lastDownTime = System.currentTimeMillis();
		lastTapX = x;
		lastTapY = y;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (Math.abs(x - lastTapX) > SNAP_DIST
				|| Math.abs(y - lastTapY) > SNAP_DIST) {
			releaseLongPress();
			lastDownTime = Long.MAX_VALUE;
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
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
		releaseLongPress();
		lastDownTime = Long.MAX_VALUE;
	}
}
