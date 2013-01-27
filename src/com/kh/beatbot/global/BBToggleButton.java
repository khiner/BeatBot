package com.kh.beatbot.global;

import com.kh.beatbot.view.TouchableSurfaceView;

public class BBToggleButton extends BBButton {
	boolean on = false;
	boolean touched = false;

	public BBToggleButton(TouchableSurfaceView parent) {
		super(parent);
	}

	public void setIconSource(BBIconSource iconSource) {
		defaultIcon = iconSource.defaultIcon;
		selectedIcon = iconSource.selectedIcon;
		if (on)
			currentIcon = selectedIcon;
		else
			currentIcon = defaultIcon;
	}
	
	@Override
	public void touch() {
		// don't change icon on touch event for toggle button.
		// wait for release to toggle icon
		touched = true;
	}

	@Override
	public void release() {
		touched = false;
	}

	public void toggle() {
		on = !on;
		updateIcon();
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
		updateIcon();
	}

	private void updateIcon() {
		currentIcon = on ? selectedIcon : defaultIcon;
	}
}
