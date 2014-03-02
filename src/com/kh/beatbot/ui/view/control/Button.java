package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.listener.OnLongPressListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResourceSet.State;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.LongPressableView;

public class Button extends LongPressableView {
	private OnPressListener pressListener;
	private OnReleaseListener releaseListener;
	private OnLongPressListener longPressListener;

	protected boolean enabled = true;

	public Button(ShapeGroup shapeGroup) {
		super(shapeGroup);
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

	public final void setOnLongPressListener(
			OnLongPressListener longPressListener) {
		this.longPressListener = longPressListener;
	}

	@Override
	public void press() {
		super.press();
		notifyPressed(); // always notify press events
	}

	@Override
	public void release() {
		releaseLongPress();
		super.release();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean shouldShrink() {
		return getState() == State.PRESSED || getState() == State.SELECTED;
	}

	/*
	 * Trigger a touch event (calls the onReleaseListener())
	 */
	public void trigger() {
		release();
		notifyReleased();
	}

	protected void notifyPressed() {
		if (pressListener != null) {
			pressListener.onPress(this);
		}
	}

	protected void notifyReleased() {
		if (releaseListener != null) {
			releaseListener.onRelease(this);
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (!enabled) {
			return;
		}

		super.handleActionDown(id, x, y);
		press();
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		if (!enabled) {
			return;
		}
		if (isPressed() && (isLongPressing() || longPressListener == null)) {
			// only release if long press hasn't happened yet
			notifyReleased();
		}
		super.handleActionUp(id, x, y);
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (!enabled) {
			return;
		}
		// x / y are relative to this view but containsPoint is absolute
		if (!containsPoint(this.x + x, this.y + y)) {
			if (isPressed()) { // pointer dragged away from button - signal release
				release();
			}
		} else { // pointer inside button
			if (!isPressed()) { // pointer was dragged away and back IN to button
				press();
			}
		}
	}

	@Override
	protected void longPress(int id, float x, float y) {
		if (longPressListener != null) {
			longPressListener.onLongPress(this);
		}
	}
	
	@Override
	protected void stateChanged() {
		IconResource resource = getIconResource();
		if (null != resource && null != resource.fillColor) {
			initRoundedRect();
		}
		super.stateChanged();
	}
}
