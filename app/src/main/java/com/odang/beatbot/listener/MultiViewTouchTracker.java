package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.TouchableView;

import java.util.concurrent.atomic.AtomicInteger;

public class MultiViewTouchTracker implements TouchableViewListener {
    private final TouchableViewsListener touchableViewsListener;

    public MultiViewTouchTracker(TouchableViewsListener touchableViewsListener) {
        this.touchableViewsListener = touchableViewsListener;
    }

    public void monitorViews(TouchableView... views) {
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
