package com.kh.beatbot.listener;

import java.util.concurrent.atomic.AtomicInteger;

import com.kh.beatbot.ui.view.TouchableView;

public class MultiViewTouchTracker implements TouchableViewListener {
	private final TouchableViewsListener touchableViewsListener;

	public MultiViewTouchTracker(TouchableViewsListener touchableViewsListener) {
		this.touchableViewsListener = touchableViewsListener;
	}

	public void trackViews(TouchableView ... views) {
		for (TouchableView view : views) {
			view.setListener(this);
		}
	}

	private AtomicInteger numControlsPressed = new AtomicInteger(0);

	@Override
	public void onPress(TouchableView view) {
		if (numControlsPressed.getAndIncrement() == 0) {
			touchableViewsListener.onFirstPress();
		}
	}

	@Override
	public void onRelease(TouchableView view) {
		if (numControlsPressed.decrementAndGet() == 0) {
			touchableViewsListener.onLastRelease();
		}
	}
}
