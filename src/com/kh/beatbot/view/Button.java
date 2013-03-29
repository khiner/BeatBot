package com.kh.beatbot.view;

import com.kh.beatbot.listener.BBOnClickListener;

public abstract class Button extends TouchableBBView {
	private BBOnClickListener listener;
	
	protected boolean enabled = false, touched = false;
	
	public Button(TouchableSurfaceView parent) {
		super(parent);
	}

	public final BBOnClickListener getOnClickListener() {
		return listener;
	}
	
	public final void setOnClickListener(BBOnClickListener listener) {
		this.listener = listener;
	}

	protected void touch() {
		touched = true;
		notifyClicked();
	}

	protected void release(boolean sendEvent) {
		touched = false;
		if (sendEvent)
			notifyClicked();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isTouched() {
		return touched;
	}
	
	protected final void notifyClicked() {
		if (listener != null)
			listener.onClick(this);
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		touch();
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		release(x >= 0 && x <= width && y >= 0 && y <= height);
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		if (x < 0 || x > width || y < 0 || y > height) {
			release(false);
		}
	}
}
