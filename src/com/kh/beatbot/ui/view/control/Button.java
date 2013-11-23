package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.view.TouchableView;

public abstract class Button extends TouchableView {
	private OnPressListener pressListener;
	private OnReleaseListener releaseListener;
	
	protected boolean enabled = true, pressed = false;

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
	
	public void press() {
		pressed = true;
		notifyPressed(); // always notify press events 
	}

	public void release() {
		pressed = false;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isPressed() {
		return pressed;
	}
	
	/*
	 * Trigger a touch event (calls the onReleaseListener())
	 */
	public void trigger() {
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
		press();
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		if (!enabled) {
			return;
		}
		if (pressed) {
			release();
			notifyReleased();
		}
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (!enabled) {
			return;
		}
		// x / y are relative to this view but containsPoint is absolute
		if (!containsPoint(this.x + x, this.y + y)) {
			if (pressed) { // pointer dragged away from button - signal release
				release();
			}
		} else { // pointer inside button
			if (!pressed) { // pointer was dragged away and back IN to button
				press();
			}
		}
	}
}
