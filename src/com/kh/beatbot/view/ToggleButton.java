package com.kh.beatbot.view;

import com.kh.beatbot.global.BBIconSource;

public class ToggleButton extends Button {
	boolean on = false;

	public ToggleButton(TouchableSurfaceView parent) {
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

	protected void touch() {
		currentIcon = selectedIcon;
	}

	protected void release(boolean sendEvent) {
		if (sendEvent) {
			setOn(!on);
			notifyClicked();
		} else if (!on) {
			currentIcon = defaultIcon;
		}
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
		currentIcon = on ? selectedIcon : defaultIcon;
	}
}
