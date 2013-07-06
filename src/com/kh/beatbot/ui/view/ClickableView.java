package com.kh.beatbot.ui.view;

import android.os.Handler;

public abstract class ClickableView extends TouchableView {

	Runnable longPressed = new Runnable() { 
	    public void run() {
	    	if (pointerIdToPos.isEmpty())
	    		return;
	    	int id = pointerIdToPos.keySet().iterator().next();
	    	Position pos = pointerIdToPos.get(id);
	        longPress(id, pos.x, pos.y);
	    }   
	};
	
	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	// time (in millis) for a long press in one location
	public final static long LONG_PRESS_TIME = 600;

	// if pointers go out of this radius, they will cancel taps, double taps and long presses
	public final static float SNAP_DIST = 30;
	
	// handler for timed events
	private final Handler handler = new Handler();
	
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
	public void handleActionDown(int id, float x, float y) {
		handler.postDelayed(longPressed, LONG_PRESS_TIME);
		lastDownTime = System.currentTimeMillis();
		lastTapX = x;
		lastTapY = y;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (Math.abs(x - lastTapX) > SNAP_DIST
				|| Math.abs(y - lastTapY) > SNAP_DIST) {
			handler.removeCallbacks(longPressed);
			lastDownTime = Long.MAX_VALUE;
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
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
		handler.removeCallbacks(longPressed);
		lastDownTime = Long.MAX_VALUE;
	}
}
