package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.listener.OnLongPressListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResource;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.LongPressableView;

public class Button extends LongPressableView {
	private OnPressListener pressListener;
	private OnReleaseListener releaseListener;
	private OnLongPressListener longPressListener;

	public Button(ShapeGroup shapeGroup) {
		super(shapeGroup);
		setShrinkable(true);
	}

	public final OnPressListener getOnPressListener() {
		return pressListener;
	}

	public final OnReleaseListener getOnReleaseListener() {
		return releaseListener;
	}

	public final void setOnPressListener(OnPressListener pressListener) {
		this.pressListener = pressListener;
	}

	public final void setOnReleaseListener(OnReleaseListener releaseListener) {
		this.releaseListener = releaseListener;
	}

	public final void setOnLongPressListener(OnLongPressListener longPressListener) {
		this.longPressListener = longPressListener;
	}

	@Override
	public void press() {
		super.press();
		notifyPress(); // always notify press events
	}

	@Override
	public void release() {
		releaseLongPress();
		super.release();
	}

	/*
	 * Trigger a touch event (calls the onReleaseListener())
	 */
	public void trigger() {
		release();
		notifyRelease();
	}

	protected void notifyPress() {
		if (pressListener != null) {
			pressListener.onPress(this);
		}
	}

	protected void notifyRelease() {
		if (releaseListener != null) {
			releaseListener.onRelease(this);
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (!isEnabled())
			return;

		super.handleActionDown(id, x, y);
		press();
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		if (!isEnabled())
			return;
		if (isPressed() && (isLongPressing() || longPressListener == null)) {
			// only release if long press hasn't happened yet
			notifyRelease();
		}
		super.handleActionUp(id, x, y);
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		checkPointerExit(id, x, y);
	}

	@Override
	protected void longPress(int id, float x, float y) {
		if (longPressListener != null) {
			longPressListener.onLongPress(this);
		}
	}

	@Override
	protected synchronized void stateChanged() {
		IconResource resource = getIconResource();
		if (null != resource && null != resource.fillColor) {
			initRoundedRect();
		}
		super.stateChanged();
	}
}
