package com.kh.beatbot.view;

import com.kh.beatbot.listener.BBOnClickListener;

public abstract class Button extends TouchableBBView {
	private BBOnClickListener listener;
	
	protected boolean enabled = true, pressed = false;
	
	public Button(TouchableSurfaceView parent) {
		super(parent);
	}

	public final BBOnClickListener getOnClickListener() {
		return listener;
	}
	
	public final void setOnClickListener(BBOnClickListener listener) {
		this.listener = listener;
	}
	
	protected void press() {
		pressed = true;
	}

	protected void release() {
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
	
	protected void notifyClicked() {
		if (listener != null)
			listener.onClick(this);
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
			notifyClicked();
		}
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (!enabled) {
			return;
		}
		if (x < 0 || x >= width || y < 0 || y > height) {
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
