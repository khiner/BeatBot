package com.kh.beatbot.ui.view;

import android.os.Handler;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.shape.RenderGroup;

public abstract class LongPressableView extends TouchableView {

	Runnable longPressed = new Runnable() {
		public void run() {
			if (pointersById.size() <= 0)
				return;
			int id = pointersById.keyAt(0);
			longPress(id, pointersById.get(id));
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

	protected abstract void longPress(int id, Pointer pos);

	public LongPressableView(View view) {
		this(view, view.getRenderGroup());
	}

	public LongPressableView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
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
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		lastTapX = pos.x;
		lastTapY = pos.y;
		beginLongPress();
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		if (Math.abs(pos.x - lastTapX) > SNAP_DIST || Math.abs(pos.y - lastTapY) > SNAP_DIST) {
			releaseLongPress();
		}
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		releaseLongPress();
	}
}
