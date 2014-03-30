package com.kh.beatbot.ui.view;

import android.os.Handler;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.shape.ShapeGroup;

public abstract class LongPressableView extends TouchableView {

	Runnable longPressed = new Runnable() {
		public void run() {
			if (pointerIdToPos.size() <= 0)
				return;
			int id = pointerIdToPos.keyAt(0);
			Position pos = pointerIdToPos.get(id);
			longPress(id, pos.x, pos.y);
			longPressing = false;
		}
	};

	// time (in millis) for a long press in one location
	public final static long LONG_PRESS_TIME = 600;

	// if pointers go out of this radius, they will cancel taps, double taps and
	// long presses
	public final static float SNAP_DIST = 30;

	protected float lastTapX = -1, lastTapY = -1;

	// handler for timed events
	private Handler handler;

	private boolean longPressing = false;

	protected abstract void longPress(int id, float x, float y);

	public LongPressableView() {
		this(null);
	}

	public LongPressableView(ShapeGroup shapeGroup) {
		super(shapeGroup);
		BeatBotActivity.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				handler = new Handler();
			}
		});
	}

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
		lastTapX = x;
		lastTapY = y;
		beginLongPress();
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (Math.abs(x - lastTapX) > SNAP_DIST || Math.abs(y - lastTapY) > SNAP_DIST) {
			releaseLongPress();
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		releaseLongPress();
	}
}
